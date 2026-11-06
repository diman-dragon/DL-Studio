package com.dlstudio.app.library

/** Текст файла → проекты с колодами и карточками (id свежие). JSON — дерево библиотеки, остальное — таблица. */
object ImportParser {
    fun parse(text: String, name: String): List<LibNode> {
        val first = text.trimStart().take(1)
        val json = if (first == "[" || first == "{") runCatching { ImportJson.decode(text) }.getOrNull().orEmpty() else emptyList()
        return wrap(json.ifEmpty { ImportTable.decks(text, name) }, name).withNewIds()
    }

    /** Достраивает дерево до проектов: одни карточки → проект и колода с именем файла, одни колоды → проект. */
    private fun wrap(nodes: List<LibNode>, name: String): List<LibNode> = when (depth(nodes)) {
        0 -> emptyList()
        1 -> listOf(LibNode("", name, children = listOf(LibNode("", name, children = nodes))))
        2 -> listOf(LibNode("", name, children = nodes))
        else -> nodes
    }

    private fun depth(nodes: List<LibNode>): Int = if (nodes.isEmpty()) 0 else 1 + depth(nodes.flatMap { it.children })
}
