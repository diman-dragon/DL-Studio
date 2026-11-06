package com.dlstudio.app.library

/** Содержимое узла по пути (пустой путь — верхний уровень); нет пути — пусто. */
fun List<LibNode>.childrenAt(path: List<String>): List<LibNode> =
    if (path.isEmpty()) this else findAt(path)?.children.orEmpty()
