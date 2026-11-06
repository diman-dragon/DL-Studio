package com.dlstudio.app.study

import com.dlstudio.app.library.LibNode

/**
 * Учебная сессия как неизменяемое значение. Карточка оценивается один раз за показ;
 * «Не знал» возвращает её в конец очереди, пока не будет отвечена иначе.
 */
data class StudySession(
    val queue: List<LibNode>,
    val total: Int,
    val shownAt: Long,
    val index: Int = 0,
    val flippedAt: Long = 0,
    val flipped: Boolean = false,
    val again: Int = 0,
    val hard: Int = 0,
    val know: Int = 0
) {
    val current: LibNode? get() = queue.getOrNull(index)
    val done: Boolean get() = current == null
    val finished: Int get() = hard + know

    fun flip(now: Long): StudySession = if (done || flipped) this else copy(flipped = true, flippedAt = now)

    /** Ответ на открытую карточку: новая сессия и запись для базы; до переворота ответа нет. */
    fun answer(grade: Grade, now: Long): Pair<StudySession, Answer>? {
        val card = current?.takeIf { flipped } ?: return null
        val record = Answer(card.id, grade, (flippedAt - shownAt).coerceAtLeast(0), (now - flippedAt).coerceAtLeast(0))
        val next = copy(
            queue = if (grade == Grade.AGAIN) queue + card else queue, index = index + 1, shownAt = now, flipped = false,
            again = again + if (grade == Grade.AGAIN) 1 else 0, hard = hard + if (grade == Grade.HARD) 1 else 0, know = know + if (grade == Grade.KNOW) 1 else 0
        )
        return next to record
    }

    companion object {
        fun start(cards: List<LibNode>, now: Long) = StudySession(cards, cards.size, now)
    }
}
