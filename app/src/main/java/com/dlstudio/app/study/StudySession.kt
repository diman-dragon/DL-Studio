package com.dlstudio.app.study

import com.dlstudio.app.library.LibNode

/**
 * Учебная сессия как неизменяемое значение. Порядок карточек задан при старте и не меняется:
 * листать можно вперёд и назад, карточка остаётся на месте. Оценка выводится из поведения (`AutoGrade`)
 * и записывается один раз — при первом показе перевода.
 */
data class StudySession(
    val id: String,
    val queue: List<LibNode>,
    val shownAt: Long,
    val index: Int = 0,
    val flipped: Set<String> = emptySet(),
    val hinted: Set<String> = emptySet(),
    val grades: Map<String, Grade> = emptyMap(),
    val random: Boolean = false
) {
    val total: Int get() = queue.size
    val finished: Int get() = grades.size
    val know: Int get() = grades.values.count { it == Grade.KNOW }
    val hard: Int get() = grades.values.count { it == Grade.HARD }
    val again: Int get() = grades.values.count { it == Grade.AGAIN }
    val current: LibNode? get() = queue.getOrNull(index)

    /** Переход к карточке `page` (последняя страница — итог); с этого момента идёт отсчёт времени до перевода. */
    fun goTo(page: Int, now: Long): StudySession = if (page == index) this else copy(index = page.coerceIn(0, queue.size), shownAt = now)

    fun useHint(): StudySession = current?.let { copy(hinted = hinted + it.id) } ?: this

    /**
     * Тап по карточке `cardId`: показать перевод или вернуться к слову. Переворачивается именно та карточка,
     * по которой тапнули (индекс страницы мог ещё не обновиться). Запись для базы — только при первом показе перевода.
     */
    fun flip(cardId: String, now: Long): Pair<StudySession, Answer?> {
        val at = queue.indexOfFirst { it.id == cardId }
        if (at < 0) return this to null
        val card = queue[at]
        val base = if (at == index) this else copy(index = at, shownAt = now - AutoGrade.GAVE_UP_MS)
        if (card.id in flipped) return base.copy(flipped = flipped - card.id) to null
        val shown = base.copy(flipped = flipped + card.id)
        if (card.id in grades) return shown to null
        val faceMs = (now - base.shownAt).coerceAtLeast(0)
        val grade = AutoGrade.of(faceMs, card.id in hinted)
        return shown.copy(grades = grades + (card.id to grade)) to Answer(card.id, grade, faceMs, 0)
    }

    companion object {
        fun start(id: String, cards: List<LibNode>, now: Long, random: Boolean = false) = StudySession(id, cards, now, random = random)

        fun restore(saved: SavedSession, cards: List<LibNode>, now: Long) =
            StudySession(saved.id, cards, now, saved.position.coerceIn(0, cards.size), grades = saved.grades.filterKeys { id -> cards.any { it.id == id } }, random = saved.random)
    }
}
