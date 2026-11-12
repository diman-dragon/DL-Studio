package com.dlstudio.app.library

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.dlstudio.app.storage.StorageNode
import com.dlstudio.app.storage.ensureLayout
import com.dlstudio.app.study.Answer
import com.dlstudio.app.study.CardState
import com.dlstudio.app.study.DayAgg
import com.dlstudio.app.study.DayMath
import com.dlstudio.app.study.Grade
import com.dlstudio.app.study.SavedSession
import java.io.Closeable
import java.io.File
import java.io.IOException
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

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

/** Схема базы и переход со старых версий (до 0.1.0 расписание хранилось только в журнале повторов). */
object DbSchema {
    private const val VERSION = 2
    private const val MIGRATED_MAX_INTERVAL = 6

    fun migrate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS nodes(id TEXT PRIMARY KEY,parent_id TEXT,type INTEGER,name TEXT,back TEXT,hint TEXT,explanation TEXT,example TEXT,ipa TEXT,meanings TEXT)")
        db.execSQL("CREATE TABLE IF NOT EXISTS reviews(id INTEGER PRIMARY KEY AUTOINCREMENT,card_id TEXT,seen_at INTEGER,face_ms INTEGER,back_ms INTEGER)")
        db.execSQL("CREATE TABLE IF NOT EXISTS card_state(card_id TEXT PRIMARY KEY,interval_days INTEGER,ease REAL,reps INTEGER,lapses INTEGER,due_day INTEGER,last_day INTEGER)")
        db.execSQL("CREATE TABLE IF NOT EXISTS study_sessions(id TEXT PRIMARY KEY,started_at INTEGER,scope_id TEXT,all_decks INTEGER,position INTEGER)")
        db.execSQL("CREATE TABLE IF NOT EXISTS study_session_cards(session_id TEXT,position INTEGER,card_id TEXT,PRIMARY KEY(session_id,position))")
        addNodeColumns(db)
        val old = db.version < VERSION
        if (old) addReviewColumns(db)
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_nodes_parent ON nodes(parent_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_reviews_card ON reviews(card_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_reviews_day ON reviews(day)")
        if (old) { db.beginTransaction(); try { backfill(db); db.version = VERSION; db.setTransactionSuccessful() } finally { db.endTransaction() } }
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

    private fun backfill(db: SQLiteDatabase) {
        val rows = db.rawQuery("SELECT id,card_id,seen_at FROM reviews WHERE day IS NULL ORDER BY id", null).use { c -> buildList { while (c.moveToNext()) add(Triple(c.getLong(0), c.getString(1), c.getLong(2))) } }
        rows.forEach { (id, _, seen) -> db.execSQL("UPDATE reviews SET day=?,grade=? WHERE id=?", arrayOf(DayMath.dayNumber(seen), Grade.KNOW.ordinal, id)) }
        if (!db.rawQuery("PRAGMA table_info(reviews)", null).use { c -> generateSequence { if (c.moveToNext()) c.getString(1) else null }.any { it == "ease" } }) return
        val latest = db.rawQuery("SELECT card_id,interval_days,ease,day,(SELECT COUNT(*) FROM reviews r2 WHERE r2.card_id=r.card_id) FROM reviews r WHERE id IN (SELECT MAX(id) FROM reviews GROUP BY card_id)", null)
        latest.use { c -> while (c.moveToNext()) {
            val interval = min(MIGRATED_MAX_INTERVAL, max(1, c.getDouble(1).roundToInt())); val last = c.getLong(3)
            val s = CardState(interval, c.getDouble(2).coerceIn(1.3, 2.8), min(2, c.getInt(4)), 0, last + interval, last)
            db.execSQL("INSERT OR IGNORE INTO card_state VALUES(?,?,?,?,?,?,?)", arrayOf(c.getString(0), s.intervalDays, s.ease, s.reps, s.lapses, s.dueDay, s.lastDay))
        } }
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

/** Расписание карточек (`card_state`) и журнал ответов (`reviews`); статистика читает только индексированные поля. */
class ReviewStore(private val db: SQLiteDatabase) {
    fun states(): Map<String, CardState> = db.rawQuery("SELECT card_id,interval_days,ease,reps,lapses,due_day,last_day FROM card_state", null).use { c ->
        buildMap { while (c.moveToNext()) put(c.getString(0), CardState(c.getInt(1), c.getDouble(2), c.getInt(3), c.getInt(4), c.getLong(5), c.getLong(6))) }
    }

    fun save(a: Answer, now: Long, day: Long, s: CardState) {
        db.beginTransaction()
        try {
            db.insertOrThrow("reviews", null, ContentValues().apply { put("card_id", a.cardId); put("seen_at", now); put("day", day); put("grade", a.grade.ordinal); put("face_ms", a.faceMs); put("back_ms", a.backMs) })
            db.execSQL("INSERT OR REPLACE INTO card_state VALUES(?,?,?,?,?,?,?)", arrayOf(a.cardId, s.intervalDays, s.ease, s.reps, s.lapses, s.dueDay, s.lastDay))
            db.setTransactionSuccessful()
        } finally { db.endTransaction() }
    }

    fun perDay(fromDay: Long): Map<Long, DayAgg> = db.rawQuery("SELECT day,COUNT(*),SUM(face_ms),SUM(grade>0) FROM reviews WHERE day>=? GROUP BY day", arrayOf(fromDay.toString())).use { c ->
        buildMap { while (c.moveToNext()) put(c.getLong(0), DayAgg(c.getInt(1), c.getLong(2), c.getInt(3))) }
    }

    fun activeDays(fromDay: Long): Set<Long> = db.rawQuery("SELECT DISTINCT day FROM reviews WHERE day>=?", arrayOf(fromDay.toString())).use { c -> buildSet { while (c.moveToNext()) add(c.getLong(0)) } }

    fun lastDay(): Long = db.rawQuery("SELECT COALESCE(MAX(day),0) FROM reviews", null).use { it.moveToFirst(); it.getLong(0) }

    fun forget(cardIds: List<String>) = cardIds.forEach { db.delete("card_state", "card_id=?", arrayOf(it)); db.delete("reviews", "card_id=?", arrayOf(it)) }
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
