package com.dlstudio.app.library

import android.database.sqlite.SQLiteDatabase
import com.dlstudio.app.study.CardState
import com.dlstudio.app.study.DayMath
import com.dlstudio.app.study.Grade
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/** Схема базы и переход со старых версий (до 0.1.0 расписание хранилось только в журнале повторов). */
object DbSchema {
    private const val VERSION = 2
    private const val MIGRATED_MAX_INTERVAL = 6

    fun migrate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS nodes(id TEXT PRIMARY KEY,parent_id TEXT,type INTEGER,name TEXT,back TEXT,hint TEXT,explanation TEXT,example TEXT)")
        db.execSQL("CREATE TABLE IF NOT EXISTS reviews(id INTEGER PRIMARY KEY AUTOINCREMENT,card_id TEXT,seen_at INTEGER,face_ms INTEGER,back_ms INTEGER)")
        db.execSQL("CREATE TABLE IF NOT EXISTS card_state(card_id TEXT PRIMARY KEY,interval_days INTEGER,ease REAL,reps INTEGER,lapses INTEGER,due_day INTEGER,last_day INTEGER)")
        db.execSQL("CREATE TABLE IF NOT EXISTS study_sessions(id TEXT PRIMARY KEY,started_at INTEGER,scope_id TEXT,all_decks INTEGER,position INTEGER)")
        db.execSQL("CREATE TABLE IF NOT EXISTS study_session_cards(session_id TEXT,position INTEGER,card_id TEXT,PRIMARY KEY(session_id,position))")
        val old = db.version < VERSION
        if (old) addReviewColumns(db)
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_nodes_parent ON nodes(parent_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_reviews_card ON reviews(card_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_reviews_day ON reviews(day)")
        if (old) { db.beginTransaction(); try { backfill(db); db.version = VERSION; db.setTransactionSuccessful() } finally { db.endTransaction() } }
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
