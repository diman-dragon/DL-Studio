package com.dlstudio.app.study

import com.dlstudio.app.library.LibNode
import kotlin.random.Random

/**
 * Выбор учебного режима. Половина показов (`flipShare`, по умолчанию 50%) — карточки с переворотом в обе стороны:
 * изучаемое слово и родное. Остальное — по весам среди включённых режимов. «Произношение» — только для одного-двух слов.
 */
object ModeWeights {
    private val weights = listOf(
        CardMode.CHOICE_FWD to 8, CardMode.CHOICE_REV to 24, CardMode.LISTEN to 11, CardMode.WRITE_FWD to 6,
        CardMode.WRITE_REV to 6, CardMode.PRONOUNCE to 10, CardMode.REPEAT to 8
    )

    fun eligible(card: LibNode, mode: CardMode): Boolean =
        mode != CardMode.PRONOUNCE || (card.back.isNotBlank() && card.name.trim().split(Regex("\\s+")).size <= 2)

    fun modeFor(card: LibNode, index: Int, options: StudyOptions = StudyOptions()): CardMode {
        val random = Random(card.id.hashCode() * 31L + index)
        val usable = options.modes.filter { eligible(card, it) }.ifEmpty { listOf(CardMode.FLIP_FWD) }
        val flips = usable.filter { it.flip }
        val rest = weights.filter { it.first in usable }
        if (flips.isNotEmpty() && (rest.isEmpty() || random.nextInt(100) < options.flipShare)) return flips[random.nextInt(flips.size)]
        var slot = random.nextInt(rest.sumOf { it.second })
        return rest.first { (_, w) -> if (slot < w) true else { slot -= w; false } }.first
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
