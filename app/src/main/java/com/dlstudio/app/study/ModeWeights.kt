package com.dlstudio.app.study

import com.dlstudio.app.library.LibNode
import kotlin.random.Random

/**
 * Выбор учебного режима. Половина показов (`flipShare`, по умолчанию 50%) — выбор перевода и выбор слова
 * (изучаемое → родное и родное → изучаемое поровну). Остальное — по весам среди включённых режимов. Простого «перевернуть» здесь нет: всегда нужно ответить. «Произношение» — только для одного-двух слов.
 */
object ModeWeights {
    /** Самые частые показы: слово → выбор перевода и перевод → выбор слова (карточка переворачивается после ответа). */
    private val frequent = listOf(CardMode.CHOICE_FWD, CardMode.CHOICE_REV)
    /** Веса в процентах от «остального» (при доле выбора 50% это половина показов): ввод слова — по 6, то есть по 3% всех показов. */
    private val weights = listOf(
        CardMode.LISTEN to 33, CardMode.WRITE_FWD to 6, CardMode.WRITE_REV to 6, CardMode.PRONOUNCE to 31, CardMode.REPEAT to 24
    )

    fun eligible(card: LibNode, mode: CardMode): Boolean =
        mode != CardMode.PRONOUNCE || (card.back.isNotBlank() && card.name.trim().split(Regex("\\s+")).size <= 2)

    fun modeFor(card: LibNode, index: Int, options: StudyOptions = StudyOptions()): CardMode {
        val random = Random(card.id.hashCode() * 31L + index)
        val usable = options.modes.filter { eligible(card, it) }.ifEmpty { listOf(CardMode.CHOICE_FWD) }
        val flips = usable.filter { it in frequent }
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
