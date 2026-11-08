package com.dlstudio.app.study

import com.dlstudio.app.library.LibNode
import kotlin.random.Random

/** Что произносит диктор и что нужно сказать в режимах голоса: в «Повторе» — предложение-пример (если есть, в половине случаев) или слово. */
object SpeakTarget {
    fun of(card: LibNode, mode: CardMode, index: Int): String =
        if (mode == CardMode.REPEAT && card.example.isNotBlank() && Random(card.id.hashCode() * 17L + index).nextBoolean()) card.example else card.name
}
