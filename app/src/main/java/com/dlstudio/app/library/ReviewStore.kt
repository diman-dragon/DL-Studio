package com.dlstudio.app.library

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.dlstudio.app.study.Answer
import com.dlstudio.app.study.CardState
import com.dlstudio.app.study.DayAgg

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
