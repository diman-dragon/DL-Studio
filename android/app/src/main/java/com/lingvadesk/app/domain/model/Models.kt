package com.lingvadesk.app.domain.model

/** Оценка карточки; `key` — значение в `reviews.grade` (TEXT), как в версиях 1.x–2.x. */
enum class Grade(val key: String) {
    AGAIN("again"), HARD("hard"), GOOD("good"), EASY("easy");

    /** Ступенью ниже: легко → знаю → трудно → снова. */
    fun down(): Grade = when (this) {
        EASY -> GOOD
        GOOD -> HARD
        HARD -> AGAIN
        AGAIN -> AGAIN
    }
}

/** Проект (в БД — таблица `languages`): язык, тема, даты — что угодно. `tts` — голос озвучки (BCP-47). */
data class Project(val id: Long, val name: String, val tts: String)

data class Deck(val id: Long, val projectId: Long, val name: String)

data class Card(
    val id: Long,
    val deckId: Long,
    val front: String,
    val back: String,
    val example: String? = null,
    val exampleTr: String? = null,
    val ord: Int = 0,
    /** Мс; 0 — новая карточка, ни разу не оценённая. */
    val due: Long = 0L,
    /** Дни. */
    val interval: Int = 0,
    val ease: Double = 2.5,
    val reps: Int = 0,
    val cloze: String? = null,
    val clozeAnswer: String? = null,
    val clozeTr: String? = null
) {
    val isNew: Boolean get() = due == 0L
    fun isDue(now: Long): Boolean = due in 1..now
}

/** Карточка со сведением для учёбы: голос проекта. */
data class StudyCard(val card: Card, val tts: String)

/** Строка истории повторений (`reviews`). */
data class Review(
    val cardId: Long,
    val ts: Long,
    val grade: Grade,
    val think: Long?,
    val look: Long?,
    val fresh: Boolean,
    val hinted: Boolean = false,
    val plays: Int = 0
)

data class ProjectStat(val project: Project, val decks: Int, val total: Int, val due: Int, val fresh: Int)

data class DeckStat(val deck: Deck, val total: Int, val due: Int, val fresh: Int)

data class TodayInfo(val n: Int, val current: Int, val best: Int)

data class DayCount(val label: String, val count: Int)

data class StatsSummary(
    val total: Int,
    val fresh: Int,
    val due: Int,
    val mature: Int,
    val reviewsTotal: Int,
    val retention30: Int?,          // % «знаю/легко» за 30 дней, null — повторений нет
    val avgPronunciation: Int?,     // % за 30 дней
    val last7: List<DayCount>,
    val today: TodayInfo
)

/** Результат импорта библиотеки. */
data class ImportResult(val projects: Int, val decks: Int, val added: Int, val skipped: Int)
