package com.dlstudio.app.library

/** Одно значение слова для обратной стороны карточки: перевод, смысл по-русски, пример на изучаемом языке и его перевод. */
data class Meaning(
    val translation: String = "",
    val explanation: String = "",
    val example: String = "",
    val exampleTranslation: String = ""
)
