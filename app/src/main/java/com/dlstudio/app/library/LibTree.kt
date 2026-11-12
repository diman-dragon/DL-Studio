package com.dlstudio.app.library

import java.util.UUID

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

/** Одно значение слова для обратной стороны карточки: перевод, смысл по-русски, пример на изучаемом языке и его перевод. */
data class Meaning(
    val translation: String = "",
    val explanation: String = "",
    val example: String = "",
    val exampleTranslation: String = ""
)

/** Добавляет `child` в конец узла по пути `parent` (пустой путь — на верхний уровень). */
fun List<LibNode>.addAt(parent: List<String>, child: LibNode): List<LibNode> = addAllAt(parent, listOf(child))

/** Добавляет `children` в конец узла по пути `parent` (пустой путь — на верхний уровень). */
fun List<LibNode>.addAllAt(parent: List<String>, children: List<LibNode>): List<LibNode> =
    if (parent.isEmpty()) this + children else editAt(parent) { it.copy(children = it.children + children) }

/** Меняет узел по пути из id; `transform` вернул null — узел удаляется вместе с содержимым. */
fun List<LibNode>.editAt(path: List<String>, transform: (LibNode) -> LibNode?): List<LibNode> =
    if (path.isEmpty()) this else mapNotNull { node ->
        when {
            node.id != path[0] -> node
            path.size == 1 -> transform(node)
            else -> node.copy(children = node.children.editAt(path.drop(1), transform))
        }
    }

/** Содержимое узла по пути (пустой путь — верхний уровень); нет пути — пусто. */
fun List<LibNode>.childrenAt(path: List<String>): List<LibNode> =
    if (path.isEmpty()) this else findAt(path)?.children.orEmpty()

/** Спускает дерево на `times` уровней вниз: проекты → их колоды → их карточки. */
fun List<LibNode>.descend(times: Int): List<LibNode> {
    var nodes = this
    repeat(times) { nodes = nodes.flatMap { it.children } }
    return nodes
}

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

/** Новый уникальный id узла. */
fun newId(): String = UUID.randomUUID().toString()

/** Копия дерева со свежими id у всех узлов: импорт не должен повторять id, уже лежащие в библиотеке. */
fun List<LibNode>.withNewIds(): List<LibNode> = map { it.copy(id = newId(), children = it.children.withNewIds()) }
