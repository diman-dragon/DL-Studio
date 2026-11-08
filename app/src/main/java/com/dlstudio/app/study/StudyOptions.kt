package com.dlstudio.app.study

/**
 * Что включено в учёбе: режимы карточек (хотя бы один всегда включён), кнопка проверки произношения, поздравления с анимацией
 * и доля показов «выбора перевода» и «выбора слова» (`flipShare`, %). Все изменения возвращают новое значение; последний включённый режим выключить нельзя.
 */
data class StudyOptions(
    val modes: Set<CardMode> = CardMode.studied,
    val pronounce: Boolean = true,
    val celebrate: Boolean = true,
    val flipShare: Int = DEFAULT_FLIP_SHARE
) {
    fun withMode(mode: CardMode, on: Boolean): StudyOptions {
        val next = if (on) modes + mode else modes - mode
        return if (next.isEmpty()) this else copy(modes = next)
    }

    fun isLastMode(mode: CardMode): Boolean = modes.size == 1 && mode in modes

    companion object { const val DEFAULT_FLIP_SHARE = 50 }
}
