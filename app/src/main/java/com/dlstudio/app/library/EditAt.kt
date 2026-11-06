package com.dlstudio.app.library

/** Меняет узел по пути из id; `transform` вернул null — узел удаляется вместе с содержимым. */
fun List<LibNode>.editAt(path: List<String>, transform: (LibNode) -> LibNode?): List<LibNode> =
    if (path.isEmpty()) this else mapNotNull { node ->
        when {
            node.id != path[0] -> node
            path.size == 1 -> transform(node)
            else -> node.copy(children = node.children.editAt(path.drop(1), transform))
        }
    }
