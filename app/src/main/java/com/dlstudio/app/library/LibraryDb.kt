package com.dlstudio.app.library

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.dlstudio.app.storage.StorageNode
import com.dlstudio.app.storage.ensureLayout
import com.dlstudio.app.study.Answer
import com.dlstudio.app.study.CardState
import com.dlstudio.app.study.DayAgg
import com.dlstudio.app.study.Grade
import com.dlstudio.app.study.SavedSession
import java.io.Closeable
import java.io.File
import java.io.IOException

fun databaseFile(parent: StorageNode, create: Boolean): StorageNode? {
    val dir = ensureLayout(parent).child("library") ?: return null
    return dir.child("library.db") ?: if (create) dir.createFile("library.db") else null
}

/**
 * Рабочая копия базы лежит во внутренней памяти приложения, а в выбранную папку (SAF) она выгружается
 * отложенно. Маркер `.dirty` помнит, что выгрузка не состоялась, и при следующем запуске побеждает рабочая копия.
 * Битую базу никогда не затираем пустой: при двух негодных копиях открытие завершается ошибкой.
 */
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
            stored.openInput().use { i -> tmp.outputStream().use { i.copyTo(it) } }
            if (isValid(tmp)) { local.delete(); if (!tmp.renameTo(local)) throw IOException("Не удалось заменить рабочую копию"); return local }
            tmp.delete()
            if (!saved) throw IOException("База в папке повреждена, рабочей копии нет")
        } else if (local.exists() && !saved) { throw IOException("Рабочая копия базы повреждена") }
        if (saved) markDirty()
        return local
    }

    /** Выгружает рабочую копию в папку хранения; без изменений ничего не делает. */
    fun push() {
        if (!marker.exists()) return
        remote?.openOutput()?.use { out -> local.inputStream().use { it.copyTo(out) } } ?: throw IOException("Нет файла базы")
        marker.delete()
    }

    private fun isValid(file: File): Boolean = runCatching {
        SQLiteDatabase.openDatabase(file.path, null, SQLiteDatabase.OPEN_READONLY).use { db ->
            db.rawQuery("PRAGMA quick_check", null).use { it.moveToFirst() && it.getString(0) == "ok" }
        }
    }.getOrDefault(false)
}

/** Current schema: content plus a transparent action journal. */
object DbSchema {
    private const val VERSION = 3

    fun migrate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS nodes(id TEXT PRIMARY KEY,parent_id TEXT,type INTEGER,name TEXT,back TEXT,hint TEXT,explanation TEXT,example TEXT,ipa TEXT,meanings TEXT)")
        // New learning foundation. These tables record structure and actions only;
        // no knowledge score, due date, interval, or automatic review queue exists here.
        db.execSQL("CREATE TABLE IF NOT EXISTS subjects(id TEXT PRIMARY KEY,name TEXT NOT NULL,description TEXT NOT NULL DEFAULT '',metadata TEXT NOT NULL DEFAULT '{}')")
        db.execSQL("CREATE TABLE IF NOT EXISTS courses(id TEXT PRIMARY KEY,subject_id TEXT NOT NULL,name TEXT NOT NULL,description TEXT NOT NULL DEFAULT '',structure TEXT NOT NULL,metadata TEXT NOT NULL DEFAULT '{}')")
        db.execSQL("CREATE TABLE IF NOT EXISTS topics(id TEXT PRIMARY KEY,course_id TEXT NOT NULL,name TEXT NOT NULL,description TEXT NOT NULL DEFAULT '',topic_order INTEGER NOT NULL DEFAULT 0,status TEXT NOT NULL,dependencies TEXT NOT NULL DEFAULT '[]',metadata TEXT NOT NULL DEFAULT '{}')")
        db.execSQL("CREATE TABLE IF NOT EXISTS materials(id TEXT PRIMARY KEY,topic_id TEXT NOT NULL,type TEXT NOT NULL,title TEXT NOT NULL,position INTEGER NOT NULL DEFAULT 0,payload TEXT NOT NULL DEFAULT '{}')")
        db.execSQL("CREATE TABLE IF NOT EXISTS progress_events(id TEXT PRIMARY KEY,topic_id TEXT NOT NULL,material_id TEXT,action TEXT NOT NULL,occurred_at INTEGER NOT NULL,duration_ms INTEGER,metadata TEXT NOT NULL DEFAULT '{}')")
        db.execSQL("CREATE TABLE IF NOT EXISTS motivation_events(id TEXT PRIMARY KEY,action TEXT NOT NULL,hp INTEGER NOT NULL DEFAULT 0,xp INTEGER NOT NULL DEFAULT 0,occurred_at INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE IF NOT EXISTS reviews(id INTEGER PRIMARY KEY AUTOINCREMENT,card_id TEXT,seen_at INTEGER,face_ms INTEGER,back_ms INTEGER)")
        db.execSQL("CREATE TABLE IF NOT EXISTS study_sessions(id TEXT PRIMARY KEY,started_at INTEGER,scope_id TEXT,all_decks INTEGER,position INTEGER)")
        db.execSQL("CREATE TABLE IF NOT EXISTS study_session_cards(session_id TEXT,position INTEGER,card_id TEXT,PRIMARY KEY(session_id,position))")
        addNodeColumns(db)
        val old = db.version < VERSION
        if (old) addReviewColumns(db)
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_nodes_parent ON nodes(parent_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_reviews_card ON reviews(card_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_reviews_day ON reviews(day)")
        if (old) db.version = VERSION
    }

    /** Базы до 0.1.22 без транскрипции и значений: колонки дописываются при открытии, данные не трогаются. */
    private fun addNodeColumns(db: SQLiteDatabase) {
        val have = db.rawQuery("PRAGMA table_info(nodes)", null).use { c -> buildSet { while (c.moveToNext()) add(c.getString(1)) } }
        if ("ipa" !in have) db.execSQL("ALTER TABLE nodes ADD COLUMN ipa TEXT")
        if ("meanings" !in have) db.execSQL("ALTER TABLE nodes ADD COLUMN meanings TEXT")
    }

    private fun addReviewColumns(db: SQLiteDatabase) {
        val have = db.rawQuery("PRAGMA table_info(reviews)", null).use { c -> buildSet { while (c.moveToNext()) add(c.getString(1)) } }
        if ("day" !in have) db.execSQL("ALTER TABLE reviews ADD COLUMN day INTEGER")
        if ("grade" !in have) db.execSQL("ALTER TABLE reviews ADD COLUMN grade INTEGER")
    }

}

/** База библиотеки: дерево, расписание и журнал. Не потокобезопасна — все вызовы идут под одним замком модели. */
class LibraryDatabase(context: Context, parent: StorageNode) : Closeable {
    private val sync = DatabaseSync(context, parent)
    private val db: SQLiteDatabase = SQLiteDatabase.openOrCreateDatabase(sync.prepare(), null).also(DbSchema::migrate)
    val nodes = NodeStore(db)
    val reviews = ReviewStore(db)
    val sessions = SessionStore(db)

    /** Выполняет правку и помечает базу как не выгруженную в папку хранения. */
    fun <T> write(block: () -> T): T { sync.markDirty(); return block() }

    fun push() = sync.push()

    override fun close() = db.close()
}

/** Дерево библиотеки в таблице `nodes`: чтение одним запросом, правки точечные (без стирания всего дерева). */
class NodeStore(private val db: SQLiteDatabase) {
    private class Row(val id: String, val parent: String?, val node: LibNode)

    fun load(): List<LibNode> {
        val rows = db.rawQuery("SELECT id,parent_id,name,back,hint,explanation,example,ipa,meanings FROM nodes ORDER BY rowid", null).use { c ->
            buildList { while (c.moveToNext()) add(Row(c.getString(0), c.getString(1), LibNode(c.getString(0), c.str(2), c.str(3), c.str(4), c.str(5), c.str(6), ipa = c.str(7), meanings = MeaningsJson.decode(c.str(8))))) }
        }
        val byParent = rows.groupBy { it.parent }
        fun build(parent: String?): List<LibNode> = byParent[parent].orEmpty().map { it.node.copy(children = build(it.id)) }
        return build(null)
    }

    private fun android.database.Cursor.str(i: Int) = getString(i) ?: ""

    fun insert(parentId: String?, level: Int, nodes: List<LibNode>) {
        nodes.forEach { n ->
            db.insertOrThrow("nodes", null, ContentValues().apply {
                put("id", n.id); put("parent_id", parentId); put("type", level); put("name", n.name)
                put("back", n.back); put("hint", n.hint); put("explanation", n.explanation); put("example", n.example)
                put("ipa", n.ipa); put("meanings", MeaningsJson.encode(n.meanings))
            })
            insert(n.id, level + 1, n.children)
        }
    }

    fun update(n: LibNode) {
        db.update("nodes", ContentValues().apply { put("name", n.name); put("back", n.back); put("hint", n.hint); put("explanation", n.explanation); put("example", n.example); put("ipa", n.ipa); put("meanings", MeaningsJson.encode(n.meanings)) }, "id=?", arrayOf(n.id))
    }

    /** Удаляет узел со всем содержимым; возвращает id удалённых карточек, чтобы стереть их расписание. */
    fun delete(id: String): List<String> {
        val all = mutableListOf(id); var i = 0
        while (i < all.size) { db.rawQuery("SELECT id FROM nodes WHERE parent_id=?", arrayOf(all[i++])).use { c -> while (c.moveToNext()) all += c.getString(0) } }
        val cards = all.filter { db.rawQuery("SELECT 1 FROM nodes WHERE id=? AND type=2", arrayOf(it)).use { c -> c.moveToFirst() } }
        all.forEach { db.delete("nodes", "id=?", arrayOf(it)) }
        return cards
    }

    fun cardCount(): Int = db.rawQuery("SELECT COUNT(*) FROM nodes WHERE type=2", null).use { it.moveToFirst(); it.getInt(0) }
}

/** Transparent action journal; it does not maintain a learning schedule. */
class ReviewStore(private val db: SQLiteDatabase) {
    fun states(): Map<String, CardState> = emptyMap()

    /** Records a user action only; it never creates or updates a future schedule. */
    fun record(a: Answer, now: Long, day: Long) {
        db.insertOrThrow("reviews", null, ContentValues().apply {
            put("card_id", a.cardId); put("seen_at", now); put("day", day)
            put("grade", 0); put("face_ms", a.faceMs); put("back_ms", a.backMs)
        })
    }

    fun perDay(fromDay: Long): Map<Long, DayAgg> = db.rawQuery("SELECT day,COUNT(*),SUM(face_ms),SUM(grade>0) FROM reviews WHERE day>=? GROUP BY day", arrayOf(fromDay.toString())).use { c ->
        buildMap { while (c.moveToNext()) put(c.getLong(0), DayAgg(c.getInt(1), c.getLong(2), c.getInt(3))) }
    }

    fun activeDays(fromDay: Long): Set<Long> = db.rawQuery("SELECT DISTINCT day FROM reviews WHERE day>=?", arrayOf(fromDay.toString())).use { c -> buildSet { while (c.moveToNext()) add(c.getLong(0)) } }

    fun lastDay(): Long = db.rawQuery("SELECT COALESCE(MAX(day),0) FROM reviews", null).use { it.moveToFirst(); it.getLong(0) }

    fun forget(cardIds: List<String>) = cardIds.forEach { db.delete("reviews", "card_id=?", arrayOf(it)) }
}

/** Учебные сессии в базе: состав и порядок карточек фиксируются при старте и не меняются до конца сессии. */
class SessionStore(private val db: SQLiteDatabase) {
    fun start(id: String, now: Long, cardIds: List<String>, random: Boolean = false) {
        db.beginTransaction()
        try {
            db.insertOrThrow("study_sessions", null, ContentValues().apply { put("id", id); put("started_at", now); put("position", 0); put("all_decks", if (random) 1 else 0) })
            cardIds.forEachIndexed { i, card -> db.insertOrThrow("study_session_cards", null, ContentValues().apply { put("session_id", id); put("position", i); put("card_id", card) }) }
            db.execSQL("DELETE FROM study_session_cards WHERE session_id NOT IN (SELECT id FROM study_sessions ORDER BY started_at DESC LIMIT $KEEP)")
            db.execSQL("DELETE FROM study_sessions WHERE id NOT IN (SELECT id FROM study_sessions ORDER BY started_at DESC LIMIT $KEEP)")
            db.setTransactionSuccessful()
        } finally { db.endTransaction() }
    }

    fun position(id: String, position: Int) { db.update("study_sessions", ContentValues().apply { put("position", position) }, "id=?", arrayOf(id)) }

    /** Последняя сессия с оценками из журнала, поставленными после её начала. */
    fun latest(): SavedSession? {
        val head = db.rawQuery("SELECT id,started_at,position,all_decks FROM study_sessions ORDER BY started_at DESC LIMIT 1", null).use { if (it.moveToFirst()) listOf(it.getString(0), it.getLong(1), it.getInt(2), it.getInt(3)) else null } ?: return null
        val ids = db.rawQuery("SELECT card_id FROM study_session_cards WHERE session_id=? ORDER BY position", arrayOf(head[0] as String)).use { c -> buildList { while (c.moveToNext()) add(c.getString(0)) } }
        val inSession = ids.toSet()
        val grades = db.rawQuery("SELECT card_id,grade FROM reviews WHERE seen_at>=? ORDER BY id", arrayOf(head[1].toString())).use { c ->
            buildMap { while (c.moveToNext()) if (c.getString(0) in inSession) put(c.getString(0), Grade.values()[c.getInt(1).coerceIn(0, Grade.values().size - 1)]) }
        }
        return SavedSession(head[0] as String, head[1] as Long, head[2] as Int, ids, grades, head[3] == 1)
    }

    private companion object { const val KEEP = 5 }
}
