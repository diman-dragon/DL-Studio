package com.dlstudio.app.library

/** Добавляет `children` в конец узла по пути `parent` (пустой путь — на верхний уровень). */
fun List<LibNode>.addAllAt(parent: List<String>, children: List<LibNode>): List<LibNode> =
    if (parent.isEmpty()) this + children else editAt(parent) { it.copy(children = it.children + children) }
