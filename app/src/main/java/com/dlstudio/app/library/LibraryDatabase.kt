package com.dlstudio.app.library

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.content.Context
import com.dlstudio.app.storage.StorageNode
import com.dlstudio.app.storage.ensureLayout
import java.io.Closeable
import java.io.File
import java.util.Calendar

class LibraryDatabase(private val context: Context, private val parent: StorageNode) : Closeable {
    private val file = File(context.cacheDir, "dlstudio-${parent.id.hashCode()}.db")
    private val db: SQLiteDatabase
    init {
        val stored = databaseFile(parent, false); if (stored != null) stored.openInput().use { input -> file.outputStream().use { input.copyTo(it) } } else file.delete()
        db = SQLiteDatabase.openOrCreateDatabase(file, null)
        db.execSQL("CREATE TABLE IF NOT EXISTS nodes(id TEXT PRIMARY KEY,parent_id TEXT,type INTEGER,name TEXT,back TEXT,hint TEXT,explanation TEXT,example TEXT)")
        db.execSQL("CREATE TABLE IF NOT EXISTS reviews(id INTEGER PRIMARY KEY AUTOINCREMENT,card_id TEXT,seen_at INTEGER,face_ms INTEGER,back_ms INTEGER,flips INTEGER,quality INTEGER,interval_days REAL,ease REAL,due_at INTEGER)")
        legacyFile()?.delete()
    }
    fun load(): List<LibNode> {
        fun read(parentId: String?): List<LibNode> { val c = if (parentId == null) db.rawQuery("SELECT * FROM nodes WHERE parent_id IS NULL ORDER BY rowid", null) else db.rawQuery("SELECT * FROM nodes WHERE parent_id=? ORDER BY rowid", arrayOf(parentId)); return c.use { cursor -> List(cursor.count) { if (cursor.moveToNext()) LibNode(cursor.getString(0), cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getString(6), cursor.getString(7), read(cursor.getString(0))) else error("node") } } }
        return read(null)
    }
    fun replaceTree(nodes: List<LibNode>) { db.beginTransaction(); try { db.delete("nodes", null, null); fun put(list: List<LibNode>, parentId: String?, type: Int) { list.forEach { n -> val v = ContentValues().apply { put("id", n.id); put("parent_id", parentId); put("type", type); put("name", n.name); put("back", n.back); put("hint", n.hint); put("explanation", n.explanation); put("example", n.example) }; db.insertOrThrow("nodes", null, v); put(n.children, n.id, type + 1) } }; put(nodes, null, 0); db.delete("reviews", "card_id NOT IN (SELECT id FROM nodes)", null); db.setTransactionSuccessful() } finally { db.endTransaction() }; sync() }
    fun studied(): Int = db.rawQuery("SELECT COUNT(DISTINCT card_id) FROM reviews", null).use { if (it.moveToFirst()) it.getInt(0) else 0 }
    fun dueAt(): Map<String, Long> = db.rawQuery("SELECT card_id,due_at FROM reviews WHERE id IN (SELECT MAX(id) FROM reviews GROUP BY card_id)", null).use { c -> buildMap { while (c.moveToNext()) put(c.getString(0), c.getLong(1)) } }
    fun stats(): StudyStats { val now = System.currentTimeMillis(); val due = dueAt(); val total = db.rawQuery("SELECT COUNT(*) FROM nodes WHERE type=2", null).use { it.moveToFirst(); it.getInt(0) }; val dueCount = due.values.count { it <= now }; val overdue = due.values.count { it in 1 until now }; val day = 86_400_000L; val start = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis; val reviews = db.rawQuery("SELECT seen_at,face_ms FROM reviews WHERE seen_at>=?", arrayOf((start - 13 * day).toString())); val byDay = IntArray(14); var face = 0L; var faceCount = 0; val active = mutableSetOf<Int>(); reviews.use { while (it.moveToNext()) { val delta = ((it.getLong(0) - start) / day).toInt() + 13; if (delta in byDay.indices) byDay[delta]++; face += it.getLong(1); faceCount++; active += ((it.getLong(0) - start) / day).toInt() } }; val buckets = IntArray(4); db.rawQuery("SELECT interval_days FROM reviews WHERE id IN (SELECT MAX(id) FROM reviews GROUP BY card_id)", null).use { while (it.moveToNext()) { val interval = it.getDouble(0); buckets[when { interval < 1.0 -> 0; interval < 7.0 -> 1; interval < 30.0 -> 2; else -> 3 }]++ } }; val streak = generateSequence(0) { it + 1 }.takeWhile { it < 365 && -it in active }.count(); return StudyStats(total, due.size, dueCount, overdue, byDay[13], if (faceCount == 0) 0 else face / faceCount, streak, byDay.toList(), buckets.toList()) }
    fun review(cardId: String, faceMs: Long, backMs: Long, flips: Int) { val now = System.currentTimeMillis(); val old = db.rawQuery("SELECT interval_days,ease FROM reviews WHERE card_id=? ORDER BY seen_at DESC LIMIT 1", arrayOf(cardId)).use { if (it.moveToFirst()) it.getDouble(0) to it.getDouble(1) else 0.0 to 2.5 }; val quality = if (faceMs in 1..3000 && flips <= 1) 5 else if (faceMs in 1..10000) 4 else 3; val interval = if (old.first == 0.0) 1.0 else if (old.first == 1.0) 6.0 else old.first * old.second; val ease = (old.second + .1 - (5 - quality) * .08).coerceIn(1.3, 2.8); val v = ContentValues().apply { put("card_id", cardId); put("seen_at", now); put("face_ms", faceMs); put("back_ms", backMs); put("flips", flips); put("quality", quality); put("interval_days", interval); put("ease", ease); put("due_at", now + (interval * 86_400_000).toLong()) }; db.insert("reviews", null, v); sync() }
    private fun sync() { databaseFile(parent, true)?.openOutput()?.use { out -> file.inputStream().use { it.copyTo(out) } } }
    private fun legacyFile() = ensureLayout(parent).child("library")?.child("library.json")
    override fun close() { sync(); db.close(); file.delete() }
}
