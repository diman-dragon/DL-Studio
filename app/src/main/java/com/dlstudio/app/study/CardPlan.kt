package com.dlstudio.app.study

import com.dlstudio.app.library.LibNode

/** Всё, что нужно показать для карточки на странице сессии: режим, фраза для голосовых режимов, правильный ответ и варианты. */
data class CardPlan(val mode: CardMode, val target: String, val correct: String, val options: List<String>) {
    companion object {
        /** Просмотр без вопросов (библиотека, карточка без перевода) — обычная карточка с переворотом. */
        fun of(card: LibNode, index: Int, queue: List<LibNode>, interactive: Boolean, study: StudyOptions): CardPlan {
            val mode = if (interactive) ModeWeights.modeFor(card, index, study) else CardMode.FLIP_FWD
            val correct = if (mode.reverse) card.name else card.back
            val options = if (interactive && mode.choice) ModeWeights.optionsFor(card, queue, mode.reverse, index) else emptyList()
            return CardPlan(mode, SpeakTarget.of(card, mode, index), correct, options)
        }
    }
}
