package com.dlstudio.app.library

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.dlstudio.app.storage.StorageNode
import com.dlstudio.app.storage.ensureLayout
import java.io.Closeable
import java.io.File
import java.io.IOException

fun databaseFile(parent: StorageNode, create: Boolean): StorageNode? {
    val dir = ensureLayout(parent).child("library") ?: return null
    return dir.child("library.db") ?: if (create) dir.createFile("library.db") else null
}

/** Keeps the local database safe while synchronising it with the user-selected folder. */
class DatabaseSync(context: Context, private val parent: StorageNode) {
    private val local = File(context.filesDir, "library-${parent.id.hashCode()}.db")
    private val marker = File(local.path + ".dirty")
    private val remote: StorageNode? by lazy { databaseFile(parent, true) }

    fun markDirty() { marker.createNewFile() }

    fun prepare(): File {
        val saved = local.exists() && isValid(local)
        if (saved && marker.exists()) return local
        val stored = databaseFile(parent, false)?.takeIf { it.size > 0 }
        if (stored != null) {
            val tmp = File(local.path + ".tmp")
            stored.openInput().use { input -> tmp.outputStream().use { input.copyTo(it) } }
            if (isValid(tmp)) {
                local.delete()
                if (!tmp.renameTo(local)) throw IOException("Не удалось заменить рабочую копию")
                return local
            }
            tmp.delete()
            if (!saved) throw IOException("База в папке повреждена, рабочей копии нет")
        } else if (local.exists() && !saved) throw IOException("Рабочая копия базы повреждена")
        if (saved) markDirty()
        return local
    }

    fun push() {
        if (!marker.exists()) return
        remote?.openOutput()?.use { output -> local.inputStream().use { it.copyTo(output) } }
            ?: throw IOException("Нет файла базы")
        marker.delete()
    }

    private fun isValid(file: File): Boolean = runCatching {
        SQLiteDatabase.openDatabase(file.path, null, SQLiteDatabase.OPEN_READONLY).use { db ->
            db.rawQuery("PRAGMA quick_check", null).use { it.moveToFirst() && it.getString(0) == "ok" }
        }
    }.getOrDefault(false)
}

/** Content hierarchy plus the new learning foundation. No review or scoring tables are created. */
object DbSchema {
    private const val VERSION = 3

    fun migrate(db: SQLiteDatabase) {
        listOf("reviews", "card_state", "study_sessions", "study_session_cards").forEach { table ->
            db.execSQL("DROP TABLE IF EXISTS $table")
        }
        db.execSQL("CREATE TABLE IF NOT EXISTS nodes(id TEXT PRIMARY KEY,parent_id TEXT,type INTEGER,name TEXT,back TEXT,hint TEXT,explanation TEXT,example TEXT,ipa TEXT,meanings TEXT)")
        db.execSQL("CREATE TABLE IF NOT EXISTS subjects(id TEXT PRIMARY KEY,name TEXT NOT NULL,description TEXT NOT NULL DEFAULT '',metadata TEXT NOT NULL DEFAULT '{}')")
        db.execSQL("CREATE TABLE IF NOT EXISTS courses(id TEXT PRIMARY KEY,subject_id TEXT NOT NULL,name TEXT NOT NULL,description TEXT NOT NULL DEFAULT '',structure TEXT NOT NULL,metadata TEXT NOT NULL DEFAULT '{}')")
        db.execSQL("CREATE TABLE IF NOT EXISTS topics(id TEXT PRIMARY KEY,course_id TEXT NOT NULL,name TEXT NOT NULL,description TEXT NOT NULL DEFAULT '',topic_order INTEGER NOT NULL DEFAULT 0,status TEXT NOT NULL,dependencies TEXT NOT NULL DEFAULT '[]',metadata TEXT NOT NULL DEFAULT '{}')")
        db.execSQL("CREATE TABLE IF NOT EXISTS materials(id TEXT PRIMARY KEY,topic_id TEXT NOT NULL,type TEXT NOT NULL,title TEXT NOT NULL,position INTEGER NOT NULL DEFAULT 0,payload TEXT NOT NULL DEFAULT '{}')")
        db.execSQL("CREATE TABLE IF NOT EXISTS progress_events(id TEXT PRIMARY KEY,topic_id TEXT NOT NULL,material_id TEXT,action TEXT NOT NULL,occurred_at INTEGER NOT NULL,duration_ms INTEGER,metadata TEXT NOT NULL DEFAULT '{}')")
        db.execSQL("CREATE TABLE IF NOT EXISTS motivation_events(id TEXT PRIMARY KEY,action TEXT NOT NULL,hp INTEGER NOT NULL DEFAULT 0,xp INTEGER NOT NULL DEFAULT 0,occurred_at INTEGER NOT NULL)")
        addNodeColumns(db)
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_nodes_parent ON nodes(parent_id)")
        if (db.version < VERSION) db.version = VERSION
    }

    private fun addNodeColumns(db: SQLiteDatabase) {
        val columns = db.rawQuery("PRAGMA table_info(nodes)", null).use { c -> buildSet { while (c.moveToNext()) add(c.getString(1)) } }
        if ("ipa" !in columns) db.execSQL("ALTER TABLE nodes ADD COLUMN ipa TEXT")
        if ("meanings" !in columns) db.execSQL("ALTER TABLE nodes ADD COLUMN meanings TEXT")
    }
}

class LibraryDatabase(context: Context, parent: StorageNode) : Closeable {
    private val sync = DatabaseSync(context, parent)
    private val db = SQLiteDatabase.openOrCreateDatabase(sync.prepare(), null).also(DbSchema::migrate)
    val nodes = NodeStore(db)

    fun <T> write(block: () -> T): T { sync.markDirty(); return block() }
    fun push() = sync.push()
    override fun close() = db.close()
}

/** Tree storage for editable learning content. */
class NodeStore(private val db: SQLiteDatabase) {
    private class Row(val id: String, val parent: String?, val node: LibNode)

    fun load(): List<LibNode> {
        val rows = db.rawQuery("SELECT id,parent_id,name,back,hint,explanation,example,ipa,meanings FROM nodes ORDER BY rowid", null).use { c ->
            buildList {
                while (c.moveToNext()) add(Row(c.getString(0), c.getString(1), LibNode(c.getString(0), c.text(2), c.text(3), c.text(4), c.text(5), c.text(6), ipa = c.text(7), meanings = MeaningsJson.decode(c.text(8)))))
            }
        }
        val byParent = rows.groupBy { it.parent }
        fun build(parent: String?): List<LibNode> = byParent[parent].orEmpty().map { it.node.copy(children = build(it.id)) }
        return build(null)
    }

    private fun android.database.Cursor.text(index: Int) = getString(index) ?: ""

    fun insert(parentId: String?, level: Int, nodes: List<LibNode>) = nodes.forEach { node ->
        db.insertOrThrow("nodes", null, ContentValues().apply {
            put("id", node.id); put("parent_id", parentId); put("type", level); put("name", node.name)
            put("back", node.back); put("hint", node.hint); put("explanation", node.explanation); put("example", node.example)
            put("ipa", node.ipa); put("meanings", MeaningsJson.encode(node.meanings))
        })
        insert(node.id, level + 1, node.children)
    }

    fun update(node: LibNode) {
        db.update("nodes", ContentValues().apply {
            put("name", node.name); put("back", node.back); put("hint", node.hint); put("explanation", node.explanation)
            put("example", node.example); put("ipa", node.ipa); put("meanings", MeaningsJson.encode(node.meanings))
        }, "id=?", arrayOf(node.id))
    }

    fun delete(id: String): List<String> {
        val all = mutableListOf(id)
        var index = 0
        while (index < all.size) db.rawQuery("SELECT id FROM nodes WHERE parent_id=?", arrayOf(all[index++])).use { c -> while (c.moveToNext()) all += c.getString(0) }
        val cards = all.filter { db.rawQuery("SELECT 1 FROM nodes WHERE id=? AND type=2", arrayOf(it)).use { c -> c.moveToFirst() } }
        all.forEach { db.delete("nodes", "id=?", arrayOf(it)) }
        return cards
    }
}
