package com.dlstudio.app.study

import com.dlstudio.app.library.LibNode
import kotlin.math.abs
import kotlin.random.Random

/** Выбор учебного режима по заданным весам и подбор 4 вариантов ответа. */
object ModeWeights {
    fun modeFor(card: LibNode, index: Int): CardMode {
        if (card.back.isBlank()) return CardMode.CHOICE_FWD
        val slot = abs(card.id.hashCode() * 37 + index * 17) % 55
        return when {
            slot < 8 -> CardMode.CHOICE_FWD
            slot < 32 -> CardMode.CHOICE_REV
            slot < 43 -> CardMode.LISTEN
            slot < 49 -> CardMode.WRITE_FWD
            else -> CardMode.WRITE_REV
        }
    }

    fun optionsFor(card: LibNode, queue: List<LibNode>, reverse: Boolean, index: Int): List<String> {
        val correct = if (reverse) card.name else card.back
        val pool = queue.asSequence().filter { it.id != card.id }
            .map { if (reverse) it.name else it.back }
            .filter { it.isNotBlank() && it != correct }
            .distinct().toList()
        val distractors = pool.shuffled(Random(card.id.hashCode())).take(3)
        return (distractors + correct).shuffled(Random(card.id.hashCode() + index * 31))
    }
}
