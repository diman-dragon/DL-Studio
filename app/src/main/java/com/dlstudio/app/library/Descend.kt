package com.dlstudio.app.library

/** Спускает дерево на `times` уровней вниз: проекты → их колоды → их карточки. */
fun List<LibNode>.descend(times: Int): List<LibNode> {
    var nodes = this
    repeat(times) { nodes = nodes.flatMap { it.children } }
    return nodes
}
