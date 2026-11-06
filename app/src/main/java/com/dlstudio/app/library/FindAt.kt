package com.dlstudio.app.library

/** Узел по пути из id; null, если пути нет. */
fun List<LibNode>.findAt(path: List<String>): LibNode? {
    var level = this
    var found: LibNode? = null
    for (id in path) {
        found = level.firstOrNull { it.id == id } ?: return null
        level = found.children
    }
    return found
}
