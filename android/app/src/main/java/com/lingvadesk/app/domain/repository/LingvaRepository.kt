package com.lingvadesk.app.domain.repository

import com.lingvadesk.app.domain.model.*
import com.lingvadesk.app.domain.srs.SrsState
import com.lingvadesk.app.domain.study.Limits

/** Все методы — suspend и сами переключаются на IO. Ошибки (например, дубль имени проекта) — исключения с понятным текстом. */
interface LingvaRepository {
    // проекты
    suspend fun projectStats(now: Long): List<ProjectStat>
    suspend fun insertProject(name: String, tts: String): Long
    suspend fun updateProject(id: Long, name: String, tts: String)
    suspend fun deleteProject(id: Long)

    // колоды
    suspend fun deckStats(projectId: Long?, now: Long): List<DeckStat>
    suspend fun insertDeck(projectId: Long, name: String): Long
    suspend fun updateDeck(id: Long, name: String)
    suspend fun deleteDeck(id: Long)

    // карточки
    suspend fun listCards(deckId: Long): List<Card>
    suspend fun insertCard(deckId: Long, front: String, back: String, example: String?, exampleTr: String?,
                           cloze: String? = null, clozeAnswer: String? = null, clozeTr: String? = null): Long
    suspend fun updateCard(card: Card)
    suspend fun deleteCard(id: Long)

    // учёба
    /** scope: `all` | `p:<id>` | `d:<id>`. */
    suspend fun studyCards(scope: String): List<StudyCard>
    suspend fun loadLimits(scope: String, now: Long): Limits
    suspend fun addReview(review: Review): Long
    suspend fun deleteReview(id: Long)
    suspend fun updateCardSrs(cardId: Long, state: SrsState)
    suspend fun restoreCardSrs(card: Card)
    suspend fun addPronunciation(cardId: Long, ts: Long, engine: String, transcript: String, score: Double)

    // статистика
    suspend fun loadToday(now: Long): TodayInfo
    suspend fun stats(now: Long): StatsSummary

    // настройки
    suspend fun settings(): Map<String, String>
    suspend fun setSetting(key: String, value: String)
}
