package com.lingvadesk.app.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.File

/**
 * Та же БД, что у версий 1.x–2.x: файл `lingocards-clean.db`, схема v6 (languages = проекты).
 * Room не используется: он сверяет схему побитово и при расхождении стёр бы данные пользователя.
 * WAL выключен: БД — один файл (нужно для резервных копий).
 */
class LingvaDatabase private constructor(private val app: Context) {
    private var helper: Helper? = null

    val file: File get() = app.getDatabasePath(NAME)

    @Synchronized
    fun db(): SQLiteDatabase {
        if (helper == null) {
            app.deleteDatabase(LEGACY_NAME)      // старая база с демо-данными не подключается
            helper = Helper(app)
        }
        return helper!!.writableDatabase
    }

    @Synchronized
    fun close() {
        helper?.close()
        helper = null
    }

    private class Helper(c: Context) : SQLiteOpenHelper(c, NAME, null, VERSION) {
        override fun onConfigure(db: SQLiteDatabase) {
            db.disableWriteAheadLogging()
            db.setForeignKeyConstraintsEnabled(true)
        }
        override fun onCreate(db: SQLiteDatabase) { Schema.DDL.forEach(db::execSQL) }
        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            Schema.migrations.forEachIndexed { i, sqls -> if (oldVersion < i + 2) sqls.forEach(db::execSQL) }
        }
    }

    companion object {
        const val NAME = "lingocards-clean.db"
        const val LEGACY_NAME = "lingocards.db"
        const val VERSION = 6

        @Volatile private var instance: LingvaDatabase? = null
        fun get(ctx: Context): LingvaDatabase =
            instance ?: synchronized(this) { instance ?: LingvaDatabase(ctx.applicationContext).also { instance = it } }
    }
}

object Schema {
    val DDL = listOf(
        "CREATE TABLE languages(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL UNIQUE, tts TEXT NOT NULL DEFAULT 'en-US')",
        "CREATE TABLE decks(id INTEGER PRIMARY KEY AUTOINCREMENT, language_id INTEGER NOT NULL REFERENCES languages(id) ON DELETE CASCADE, name TEXT NOT NULL, created INTEGER NOT NULL, kind TEXT NOT NULL DEFAULT 'cards')",
        "CREATE TABLE cards(id INTEGER PRIMARY KEY AUTOINCREMENT, deck_id INTEGER NOT NULL REFERENCES decks(id) ON DELETE CASCADE, front TEXT NOT NULL, back TEXT NOT NULL, example TEXT, example_tr TEXT, ord INTEGER NOT NULL DEFAULT 0, due INTEGER NOT NULL DEFAULT 0, interval INTEGER NOT NULL DEFAULT 0, ease REAL NOT NULL DEFAULT 2.5, reps INTEGER NOT NULL DEFAULT 0, speaker INTEGER NOT NULL DEFAULT 0, cloze TEXT, cloze_answer TEXT, cloze_tr TEXT)",
        "CREATE INDEX idx_cards_deck ON cards(deck_id, ord)",
        "CREATE TABLE settings(key TEXT PRIMARY KEY, value TEXT)",
        "CREATE TABLE reviews(id INTEGER PRIMARY KEY AUTOINCREMENT, card_id INTEGER NOT NULL REFERENCES cards(id) ON DELETE CASCADE, ts INTEGER NOT NULL, grade TEXT NOT NULL, think INTEGER, look INTEGER, fresh INTEGER NOT NULL DEFAULT 0, mode TEXT NOT NULL DEFAULT 'srs', hinted INTEGER NOT NULL DEFAULT 0, plays INTEGER NOT NULL DEFAULT 0)",
        "CREATE INDEX idx_reviews_ts ON reviews(ts)",
        "CREATE INDEX idx_reviews_card ON reviews(card_id)",
        "CREATE TABLE pronunciations(id INTEGER PRIMARY KEY AUTOINCREMENT, card_id INTEGER NOT NULL REFERENCES cards(id) ON DELETE CASCADE, ts INTEGER NOT NULL, engine TEXT, transcript TEXT, score REAL, audio TEXT)",
        "CREATE INDEX idx_pron_card ON pronunciations(card_id, ts)",
        "CREATE TABLE games(id INTEGER PRIMARY KEY AUTOINCREMENT, ts INTEGER NOT NULL, scope TEXT NOT NULL, score INTEGER NOT NULL, correct INTEGER NOT NULL, wrong INTEGER NOT NULL, best_combo INTEGER NOT NULL DEFAULT 0, seconds INTEGER NOT NULL DEFAULT 0)",
        "CREATE INDEX idx_games_ts ON games(ts)"
    )

    /** Миграции 1→2, 2→3, … (индекс i — переход на версию i+2). */
    val migrations: List<List<String>> = listOf(
        listOf(
            "CREATE TABLE IF NOT EXISTS reviews(id INTEGER PRIMARY KEY AUTOINCREMENT, card_id INTEGER NOT NULL REFERENCES cards(id) ON DELETE CASCADE, ts INTEGER NOT NULL, grade TEXT NOT NULL, think INTEGER, look INTEGER, fresh INTEGER NOT NULL DEFAULT 0)",
            "CREATE INDEX IF NOT EXISTS idx_reviews_ts ON reviews(ts)",
            "CREATE INDEX IF NOT EXISTS idx_reviews_card ON reviews(card_id)",
            "DROP TABLE IF EXISTS deck_state"
        ),
        listOf(
            "ALTER TABLE reviews ADD COLUMN mode TEXT NOT NULL DEFAULT 'srs'",
            "ALTER TABLE reviews ADD COLUMN hinted INTEGER NOT NULL DEFAULT 0",
            "ALTER TABLE reviews ADD COLUMN plays INTEGER NOT NULL DEFAULT 0",
            "CREATE TABLE IF NOT EXISTS pronunciations(id INTEGER PRIMARY KEY AUTOINCREMENT, card_id INTEGER NOT NULL REFERENCES cards(id) ON DELETE CASCADE, ts INTEGER NOT NULL, engine TEXT, transcript TEXT, score REAL, audio TEXT)",
            "CREATE INDEX IF NOT EXISTS idx_pron_card ON pronunciations(card_id, ts)",
            "DELETE FROM settings WHERE key LIKE 'session:%' OR key IN ('today','scope')"
        ),
        listOf(
            "CREATE TABLE IF NOT EXISTS games(id INTEGER PRIMARY KEY AUTOINCREMENT, ts INTEGER NOT NULL, scope TEXT NOT NULL, score INTEGER NOT NULL, correct INTEGER NOT NULL, wrong INTEGER NOT NULL, best_combo INTEGER NOT NULL DEFAULT 0, seconds INTEGER NOT NULL DEFAULT 0)",
            "CREATE INDEX IF NOT EXISTS idx_games_ts ON games(ts)"
        ),
        listOf(
            "ALTER TABLE decks ADD COLUMN kind TEXT NOT NULL DEFAULT 'cards'",
            "ALTER TABLE cards ADD COLUMN speaker INTEGER NOT NULL DEFAULT 0"
        ),
        listOf(
            "ALTER TABLE cards ADD COLUMN cloze TEXT",
            "ALTER TABLE cards ADD COLUMN cloze_answer TEXT",
            "ALTER TABLE cards ADD COLUMN cloze_tr TEXT"
        )
    )
}
