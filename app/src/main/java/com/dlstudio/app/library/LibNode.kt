package com.dlstudio.app.library

/**
 * Узел библиотеки. Глубина задаёт тип: 0 — проект (например «Английский B1»), 1 — колода,
 * 2 — карточка (`name` — лицевая сторона, `back` — главный перевод для списков). У карточки ещё `ipa` (транскрипция)
 * и `meanings` (значения для рамок на обороте); оба поля необязательны, без них карточка открывается по-старому.
 */
data class LibNode(
    val id: String,
    val name: String,
    val back: String = "",
    val hint: String = "",
    val explanation: String = "",
    val example: String = "",
    val children: List<LibNode> = emptyList(),
    val ipa: String = "",
    val meanings: List<Meaning> = emptyList()
) {
    /** Подсказка на изучаемом языке: своя (`hint`), а если её нет — пример из первого значения (то же предложение видно на обороте). */
    val hintText: String get() = hint.ifBlank { meanings.firstOrNull()?.example.orEmpty() }
}
