package com.dlstudio.app.library

/** Добавляет `child` в конец узла по пути `parent` (пустой путь — на верхний уровень). */
fun List<LibNode>.addAt(parent: List<String>, child: LibNode): List<LibNode> = addAllAt(parent, listOf(child))
