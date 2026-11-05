package com.lingvadesk.app.data.repository

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.lingvadesk.app.data.local.LingvaDatabase
import com.lingvadesk.app.domain.model.*
import com.lingvadesk.app.domain.repository.LingvaRepository
import com.lingvadesk.app.domain.srs.SrsState
import com.lingvadesk.app.domain.study.DeckPrefs
import com.lingvadesk.app.domain.study.Limits
import com.lingvadesk.app.domain.study.Streaks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LingvaRepositoryImpl @Inject constructor(private val database: LingvaDatabase) : LingvaRepository {

    private suspend fun <T> io(block: (SQLiteDatabase) -> T): T = withContext(Dispatchers.IO) { block(database.db()) }

    private fun Cursor.str(n: String): String? = getColumnIndexOrThrow(n).let { if (isNull(it)) null else getString(it) }
    private fun Cursor.lng(n: String): Long = getLong(getColumnIndexOrThrow(n))
    private fun Cursor.int(n: String): Int = getInt(getColumnIndexOrThrow(n))
    private fun Cursor.dbl(n: String): Double = getDouble(getColumnIndexOrThrow(n))

    private fun Cursor.card() = Card(
        id = lng("id"), deckId = lng("deck_id"), front = str("front") ?: "", back = str("back") ?: "",
        example = str("example"), exampleTr = str("example_tr"), ord = int("ord"), due = lng("due"),
        interval = int("interval"), ease = dbl("ease"), reps = int("reps"),
        cloze = str("cloze"), clozeAnswer = str("cloze_answer"), clozeTr = str("cloze_tr")
    )

    private fun SQLiteDatabase.count(sql: String, vararg args: String): Int =
        rawQuery(sql, args).use { if (it.moveToFirst()) it.getInt(0) else 0 }

    private inline fun <T> SQLiteDatabase.tx(block: () -> T): T {
        beginTransaction()
        try { val r = block(); setTransactionSuccessful(); return r } finally { endTransaction() }
    }

    // ---------- проекты ----------
    override suspend fun projectStats(now: Long): List<ProjectStat> = io { db ->
        val sql = """
            SELECT l.id, l.name, l.tts,
              (SELECT COUNT(*) FROM decks d WHERE d.language_id = l.id) AS decks,
              (SELECT COUNT(*) FROM cards c JOIN decks d ON d.id = c.deck_id WHERE d.language_id = l.id) AS total,
              (SELECT COUNT(*) FROM cards c JOIN decks d ON d.id = c.deck_id WHERE d.language_id = l.id AND c.due > 0 AND c.due <= $now) AS due,
              (SELECT COUNT(*) FROM cards c JOIN decks d ON d.id = c.deck_id WHERE d.language_id = l.id AND c.due = 0) AS fresh
            FROM languages l ORDER BY l.name COLLATE NOCASE"""
        db.rawQuery(sql, null).use { c ->
            buildList { while (c.moveToNext()) add(ProjectStat(Project(c.lng("id"), c.str("name") ?: "", c.str("tts") ?: "en-US"), c.int("decks"), c.int("total"), c.int("due"), c.int("fresh"))) }
        }
    }

    override suspend fun insertProject(name: String, tts: String): Long = io { db ->
        val n = name.trim()
        require(n.isNotEmpty()) { "Введите название проекта" }
        if (db.count("SELECT COUNT(*) FROM languages WHERE name = ?", n) > 0) throw IllegalArgumentException("Проект «$n» уже есть")
        db.insertOrThrow("languages", null, ContentValues().apply { put("name", n); put("tts", tts.ifBlank { "en-US" }) })
    }

    override suspend fun updateProject(id: Long, name: String, tts: String) {
        io { db ->
            val n = name.trim()
            require(n.isNotEmpty()) { "Введите название проекта" }
            if (db.count("SELECT COUNT(*) FROM languages WHERE name = ? AND id <> ?", n, id.toString()) > 0) throw IllegalArgumentException("Проект «$n» уже есть")
            db.update("languages", ContentValues().apply { put("name", n); put("tts", tts.ifBlank { "en-US" }) }, "id = ?", arrayOf(id.toString()))
        }
    }

    override suspend fun deleteProject(id: Long) {
        io { db ->
            db.tx {
                val deckIds = db.rawQuery("SELECT id FROM decks WHERE language_id = ?", arrayOf(id.toString())).use { c -> buildList { while (c.moveToNext()) add(c.getLong(0)) } }
                deckIds.forEach { db.delete("settings", "key LIKE ?", arrayOf("deck:$it:%")) }
                db.delete("languages", "id = ?", arrayOf(id.toString()))   // колоды, карточки, история — каскадом
            }
        }
    }

    // ---------- колоды ----------
    override suspend fun deckStats(projectId: Long?, now: Long): List<DeckStat> = io { db ->
        val where = if (projectId == null) "" else "WHERE d.language_id = $projectId"
        val sql = """
            SELECT d.id, d.language_id, d.name,
              COUNT(c.id) AS total,
              COALESCE(SUM(CASE WHEN c.due > 0 AND c.due <= $now THEN 1 ELSE 0 END), 0) AS due,
              COALESCE(SUM(CASE WHEN c.due = 0 THEN 1 ELSE 0 END), 0) AS fresh
            FROM decks d LEFT JOIN cards c ON c.deck_id = d.id $where
            GROUP BY d.id ORDER BY d.language_id, d.name COLLATE NOCASE"""
        db.rawQuery(sql, null).use { c ->
            buildList { while (c.moveToNext()) add(DeckStat(Deck(c.lng("id"), c.lng("language_id"), c.str("name") ?: ""), c.int("total"), c.int("due"), c.int("fresh"))) }
        }
    }

    override suspend fun insertDeck(projectId: Long, name: String): Long = io { db ->
        require(name.isNotBlank()) { "Введите название колоды" }
        db.insertOrThrow("decks", null, ContentValues().apply {
            put("language_id", projectId); put("name", name.trim()); put("created", System.currentTimeMillis())
        })
    }

    override suspend fun updateDeck(id: Long, name: String) {
        io { db ->
            require(name.isNotBlank()) { "Введите название колоды" }
            db.update("decks", ContentValues().apply { put("name", name.trim()) }, "id = ?", arrayOf(id.toString()))
        }
    }

    override suspend fun deleteDeck(id: Long) {
        io { db ->
            db.tx {
                db.delete("settings", "key LIKE ?", arrayOf("deck:$id:%"))
                db.delete("decks", "id = ?", arrayOf(id.toString()))
            }
        }
    }

    // ---------- карточки ----------
    override suspend fun listCards(deckId: Long): List<Card> = io { db ->
        db.rawQuery("SELECT * FROM cards WHERE deck_id = ? ORDER BY ord, id", arrayOf(deckId.toString())).use { c ->
            buildList { while (c.moveToNext()) add(c.card()) }
        }
    }

    override suspend fun insertCard(deckId: Long, front: String, back: String, example: String?, exampleTr: String?,
                                    cloze: String?, clozeAnswer: String?, clozeTr: String?): Long = io { db ->
        require(front.isNotBlank() && back.isNotBlank()) { "Заполните слово и перевод" }
        val ord = db.count("SELECT COALESCE(MAX(ord), -1) + 1 FROM cards WHERE deck_id = ?", deckId.toString())
        db.insertOrThrow("cards", null, ContentValues().apply {
            put("deck_id", deckId); put("front", front.trim()); put("back", back.trim())
            put("example", example?.trim()?.ifEmpty { null }); put("example_tr", exampleTr?.trim()?.ifEmpty { null })
            put("cloze", cloze?.trim()?.ifEmpty { null }); put("cloze_answer", clozeAnswer?.trim()?.ifEmpty { null })
            put("cloze_tr", clozeTr?.trim()?.ifEmpty { null }); put("ord", ord)
        })
    }

    override suspend fun updateCard(card: Card) {
        io { db ->
            require(card.front.isNotBlank() && card.back.isNotBlank()) { "Заполните слово и перевод" }
            db.update("cards", ContentValues().apply {
                put("front", card.front.trim()); put("back", card.back.trim())
                put("example", card.example?.trim()?.ifEmpty { null }); put("example_tr", card.exampleTr?.trim()?.ifEmpty { null })
                put("cloze", card.cloze?.trim()?.ifEmpty { null }); put("cloze_answer", card.clozeAnswer?.trim()?.ifEmpty { null })
                put("cloze_tr", card.clozeTr?.trim()?.ifEmpty { null })
            }, "id = ?", arrayOf(card.id.toString()))
        }
    }

    override suspend fun deleteCard(id: Long) { io { it.delete("cards", "id = ?", arrayOf(id.toString())) } }

    // ---------- учёба ----------
    private fun scopeClause(scope: String): String {
        val id = scope.substringAfter(':', "").toLongOrNull()
        return when {
            scope.startsWith("p:") && id != null -> "d.language_id = $id"
            scope.startsWith("d:") && id != null -> "d.id = $id"
            else -> "1 = 1"
        }
    }

    override suspend fun studyCards(scope: String): List<StudyCard> = io { db ->
        val sql = """
            SELECT c.*, l.tts AS tts
            FROM cards c JOIN decks d ON d.id = c.deck_id JOIN languages l ON l.id = d.language_id
            WHERE ${scopeClause(scope)} ORDER BY d.id, c.ord, c.id"""
        db.rawQuery(sql, null).use { c ->
            buildList { while (c.moveToNext()) add(StudyCard(c.card(), c.str("tts") ?: "en-US")) }
        }
    }

    private fun startOfDay(now: Long): Long = Calendar.getInstance().apply {
        timeInMillis = now; set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    override suspend fun loadLimits(scope: String, now: Long): Limits = io { db ->
        val settings = readSettings(db)
        val deckIds = db.rawQuery("SELECT d.id FROM decks d WHERE ${scopeClause(scope)}", null).use { c -> buildList { while (c.moveToNext()) add(c.getLong(0)) } }
        val used = HashMap<Long, Int>()
        db.rawQuery("SELECT c.deck_id, COUNT(*) FROM reviews r JOIN cards c ON c.id = r.card_id WHERE r.fresh = 1 AND r.ts >= ${startOfDay(now)} GROUP BY c.deck_id", null).use { c ->
            while (c.moveToNext()) used[c.getLong(0)] = c.getInt(1)
        }
        DeckPrefs.limitsFrom(settings, deckIds, used)
    }

    override suspend fun addReview(review: Review): Long = io { db ->
        db.insertOrThrow("reviews", null, ContentValues().apply {
            put("card_id", review.cardId); put("ts", review.ts); put("grade", review.grade.key)
            put("think", review.think); put("look", review.look); put("fresh", if (review.fresh) 1 else 0)
            put("hinted", if (review.hinted) 1 else 0); put("plays", review.plays)
        })
    }

    override suspend fun deleteReview(id: Long) { io { it.delete("reviews", "id = ?", arrayOf(id.toString())) } }

    override suspend fun updateCardSrs(cardId: Long, state: SrsState) {
        io { db ->
            db.update("cards", ContentValues().apply {
                put("due", state.due); put("interval", state.interval); put("ease", state.ease); put("reps", state.reps)
            }, "id = ?", arrayOf(cardId.toString()))
        }
    }

    override suspend fun restoreCardSrs(card: Card) = updateCardSrs(card.id, SrsState(card.ease, card.interval, card.reps, card.due))

    override suspend fun addPronunciation(cardId: Long, ts: Long, engine: String, transcript: String, score: Double) {
        io { db ->
            db.insertOrThrow("pronunciations", null, ContentValues().apply {
                put("card_id", cardId); put("ts", ts); put("engine", engine); put("transcript", transcript); put("score", score)
            })
        }
    }

    // ---------- статистика ----------
    private fun dayNumber(db: SQLiteDatabase, ts: Long): Long =
        db.rawQuery("SELECT CAST(julianday(date($ts / 1000, 'unixepoch', 'localtime')) AS INTEGER)", null).use { it.moveToFirst(); it.getLong(0) }

    private fun today(db: SQLiteDatabase, now: Long): TodayInfo {
        val n = db.count("SELECT COUNT(*) FROM reviews WHERE ts >= ${startOfDay(now)}")
        val days = db.rawQuery("SELECT DISTINCT CAST(julianday(date(ts / 1000, 'unixepoch', 'localtime')) AS INTEGER) AS d FROM reviews ORDER BY d", null)
            .use { c -> buildList { while (c.moveToNext()) add(c.getLong(0)) } }
        return Streaks.compute(days, dayNumber(db, now), n)
    }

    override suspend fun loadToday(now: Long): TodayInfo = io { today(it, now) }

    override suspend fun stats(now: Long): StatsSummary = io { db ->
        val since30 = now - 30L * 86_400_000L
        val good30 = db.count("SELECT COUNT(*) FROM reviews WHERE ts >= $since30 AND grade IN ('good','easy')")
        val all30 = db.count("SELECT COUNT(*) FROM reviews WHERE ts >= $since30")
        val pron = db.rawQuery("SELECT AVG(score) FROM pronunciations WHERE ts >= $since30 AND score IS NOT NULL", null).use { if (it.moveToFirst() && !it.isNull(0)) (it.getDouble(0) * 100).toInt() else null }
        val perDay = HashMap<Long, Int>()
        val todayNo = dayNumber(db, now)
        db.rawQuery("SELECT CAST(julianday(date(ts / 1000, 'unixepoch', 'localtime')) AS INTEGER) AS d, COUNT(*) FROM reviews WHERE ts >= ${now - 8L * 86_400_000L} GROUP BY d", null)
            .use { c -> while (c.moveToNext()) perDay[c.getLong(0)] = c.getInt(1) }
        val last7 = (6 downTo 0).map { back ->
            val cal = Calendar.getInstance().apply { timeInMillis = now; add(Calendar.DAY_OF_YEAR, -back) }
            DayCount("%02d.%02d".format(cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH) + 1), perDay[todayNo - back] ?: 0)
        }
        StatsSummary(
            total = db.count("SELECT COUNT(*) FROM cards"),
            fresh = db.count("SELECT COUNT(*) FROM cards WHERE due = 0"),
            due = db.count("SELECT COUNT(*) FROM cards WHERE due > 0 AND due <= $now"),
            mature = db.count("SELECT COUNT(*) FROM cards WHERE interval >= 21"),
            reviewsTotal = db.count("SELECT COUNT(*) FROM reviews"),
            retention30 = if (all30 == 0) null else good30 * 100 / all30,
            avgPronunciation = pron,
            last7 = last7,
            today = today(db, now)
        )
    }

    // ---------- настройки ----------
    private fun readSettings(db: SQLiteDatabase): Map<String, String> =
        db.rawQuery("SELECT key, value FROM settings", null).use { c ->
            buildMap { while (c.moveToNext()) put(c.getString(0), c.getString(1) ?: "") }
        }

    override suspend fun settings(): Map<String, String> = io { readSettings(it) }

    override suspend fun setSetting(key: String, value: String) {
        io { it.execSQL("INSERT OR REPLACE INTO settings(key, value) VALUES(?, ?)", arrayOf(key, value)) }
    }
}
