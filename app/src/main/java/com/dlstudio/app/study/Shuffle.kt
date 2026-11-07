package com.dlstudio.app.study

import com.dlstudio.app.library.LibNode
import kotlin.random.Random

/** Случайный порядок для круга карточек; новый круг никогда не повторяет прежний порядок. */
object Shuffle {
    fun differentOrder(cards: List<LibNode>, previous: List<String>, random: Random = Random.Default): List<LibNode> {
        if (cards.size < 2) return cards
        repeat(TRIES) { val order = cards.shuffled(random); if (order.map { it.id } != previous) return order }
        return cards.drop(1) + cards.first()
    }

    private const val TRIES = 10
}
