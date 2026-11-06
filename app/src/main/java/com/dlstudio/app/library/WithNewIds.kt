package com.dlstudio.app.library

/** Копия дерева со свежими id у всех узлов: импорт не должен повторять id, уже лежащие в библиотеке. */
fun List<LibNode>.withNewIds(): List<LibNode> = map { it.copy(id = newId(), children = it.children.withNewIds()) }
