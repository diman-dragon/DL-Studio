package com.dlstudio.app.library

/**
 * Узел библиотеки. Глубина задаёт тип: 0 — проект (например «Английский B1»), 1 — колода,
 * 2 — карточка (`name` — лицевая сторона, `back` — обратная).
 */
data class LibNode(
    val id: String,
    val name: String,
    val back: String = "",
    val hint: String = "",
    val explanation: String = "",
    val example: String = "",
    val children: List<LibNode> = emptyList()
)
