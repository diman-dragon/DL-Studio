package com.dlstudio.app.ui.library

import com.dlstudio.app.library.LibNode

/** Сколько карточек лежит в проекте (уровень 0) или колоде (уровень 1); у самой карточки — 0. */
fun cards(node: LibNode, level: Int): Int = when (level) {
    0 -> node.children.sumOf { it.children.size }
    1 -> node.children.size
    else -> 0
}
