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
    val grades: Map<String, Grade> = emptyMap()
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

    /** Тап по карточке: показать перевод или вернуться к слову. Запись для базы — только при первом показе перевода. */
    fun flip(now: Long): Pair<StudySession, Answer?> {
        val card = current ?: return this to null
        if (card.id in flipped) return copy(flipped = flipped - card.id) to null
        val shown = copy(flipped = flipped + card.id)
        if (card.id in grades) return shown to null
        val faceMs = (now - shownAt).coerceAtLeast(0)
        val grade = AutoGrade.of(faceMs, card.id in hinted)
        return shown.copy(grades = grades + (card.id to grade)) to Answer(card.id, grade, faceMs, 0)
    }

    companion object {
        fun start(id: String, cards: List<LibNode>, now: Long) = StudySession(id, cards, now)

        fun restore(saved: SavedSession, cards: List<LibNode>, now: Long) =
            StudySession(saved.id, cards, now, saved.position.coerceIn(0, cards.size), grades = saved.grades.filterKeys { id -> cards.any { it.id == id } })
    }
}
