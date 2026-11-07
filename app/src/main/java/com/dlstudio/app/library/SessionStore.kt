package com.dlstudio.app.library

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.dlstudio.app.study.Grade
import com.dlstudio.app.study.SavedSession

/** Учебные сессии в базе: состав и порядок карточек фиксируются при старте и не меняются до конца сессии. */
class SessionStore(private val db: SQLiteDatabase) {
    fun start(id: String, now: Long, cardIds: List<String>) {
        db.beginTransaction()
        try {
            db.insertOrThrow("study_sessions", null, ContentValues().apply { put("id", id); put("started_at", now); put("position", 0) })
            cardIds.forEachIndexed { i, card -> db.insertOrThrow("study_session_cards", null, ContentValues().apply { put("session_id", id); put("position", i); put("card_id", card) }) }
            db.execSQL("DELETE FROM study_session_cards WHERE session_id NOT IN (SELECT id FROM study_sessions ORDER BY started_at DESC LIMIT $KEEP)")
            db.execSQL("DELETE FROM study_sessions WHERE id NOT IN (SELECT id FROM study_sessions ORDER BY started_at DESC LIMIT $KEEP)")
            db.setTransactionSuccessful()
        } finally { db.endTransaction() }
    }

    fun position(id: String, position: Int) { db.update("study_sessions", ContentValues().apply { put("position", position) }, "id=?", arrayOf(id)) }

    /** Последняя сессия с оценками из журнала, поставленными после её начала. */
    fun latest(): SavedSession? {
        val head = db.rawQuery("SELECT id,started_at,position FROM study_sessions ORDER BY started_at DESC LIMIT 1", null).use { if (it.moveToFirst()) Triple(it.getString(0), it.getLong(1), it.getInt(2)) else null } ?: return null
        val ids = db.rawQuery("SELECT card_id FROM study_session_cards WHERE session_id=? ORDER BY position", arrayOf(head.first)).use { c -> buildList { while (c.moveToNext()) add(c.getString(0)) } }
        val inSession = ids.toSet()
        val grades = db.rawQuery("SELECT card_id,grade FROM reviews WHERE seen_at>=? ORDER BY id", arrayOf(head.second.toString())).use { c ->
            buildMap { while (c.moveToNext()) if (c.getString(0) in inSession) put(c.getString(0), Grade.values()[c.getInt(1).coerceIn(0, Grade.values().size - 1)]) }
        }
        return SavedSession(head.first, head.second, head.third, ids, grades)
    }

    private companion object { const val KEEP = 5 }
}
