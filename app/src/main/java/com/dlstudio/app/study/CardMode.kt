package com.dlstudio.app.study

/**
 * Режим показа карточки в сессии. `reverse` — на лицевой стороне родное слово (перевод), на обороте изучаемое;
 * `speaks` — отвечают голосом в микрофон. Режим выбирается через [ModeWeights].
 */
enum class CardMode(val reverse: Boolean = false, val speaks: Boolean = false) {
    /** Изучаемое слово, тап — перевод. */
    FLIP_FWD,
    /** Родное слово, тап — изучаемое слово. */
    FLIP_REV(reverse = true),
    /** Показывается иностранное слово → 4 варианта перевода на родном языке. */
    CHOICE_FWD,
    /** Показывается родное слово → 4 варианта иностранного. */
    CHOICE_REV(reverse = true),
    /** Произносится слово → 4 варианта; текст скрыт, через 4 с появляется подсказка. */
    LISTEN,
    /** Пользователь пишет перевод. */
    WRITE_FWD,
    /** Пользователь пишет слово на иностранном языке. */
    WRITE_REV(reverse = true),
    /** Показано родное слово (одно-два слова): нужно произнести его на изучаемом языке. */
    PRONOUNCE(reverse = true, speaks = true),
    /** Диктор произносит слово или предложение: нужно повторить за ним. */
    REPEAT(speaks = true);

    val flip: Boolean get() = this == FLIP_FWD || this == FLIP_REV
    val typed: Boolean get() = this == WRITE_FWD || this == WRITE_REV
    val choice: Boolean get() = !flip && !typed && !speaks
}
