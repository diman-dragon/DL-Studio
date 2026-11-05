package com.lingvadesk.app.domain.usecase

import com.lingvadesk.app.domain.model.Card
import javax.inject.Inject

data class StudySessionItem(
    val card: Card,
    val mode: String,
    val choices: List<String> = emptyList()
)

class StudySessionUseCase @Inject constructor() {

    fun buildSession(cards: List<Card>, sessionMax: Int = 20): List<StudySessionItem> {
        val now = System.currentTimeMillis()
        val dueCards = cards.filter { it.due <= now }.shuffled()
        val freshCards = cards.filter { it.interval == 0 && it.due > now }.shuffled()

        val queue = (dueCards + freshCards).take(sessionMax)

        return queue.mapIndexed { index, card ->
            val mode = when (index % 4) {
                0 -> "flip"
                1 -> "quiz"
                2 -> "listen"
                else -> "speak"
            }
            val choices = if (mode == "quiz") {
                val incorrect = cards.filter { it.id != card.id }.shuffled().take(3).map { it.back }
                (incorrect + card.back).shuffled()
            } else {
                emptyList()
            }
            StudySessionItem(card = card, mode = mode, choices = choices)
        }
    }
}
