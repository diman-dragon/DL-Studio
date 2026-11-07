package com.dlstudio.app.study

import com.dlstudio.app.library.LibNode
import kotlin.math.abs
import kotlin.random.Random

/** Выбор учебного режима по весам среди включённых в настройках и подбор 4 вариантов ответа. */
object ModeWeights {
    private val weights = listOf(CardMode.CHOICE_FWD to 8, CardMode.CHOICE_REV to 24, CardMode.LISTEN to 11, CardMode.WRITE_FWD to 6, CardMode.WRITE_REV to 6)

    fun modeFor(card: LibNode, index: Int, enabled: Set<CardMode> = CardMode.values().toSet()): CardMode {
        val pool = weights.filter { it.first in enabled }.ifEmpty { weights }
        var slot = abs(card.id.hashCode() * 37 + index * 17) % pool.sumOf { it.second }
        return pool.first { (_, w) -> if (slot < w) true else { slot -= w; false } }.first
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
