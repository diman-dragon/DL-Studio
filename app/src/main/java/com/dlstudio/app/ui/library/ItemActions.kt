package com.dlstudio.app.ui.library

import com.dlstudio.app.library.LibNode

/** Что можно сделать с элементом: открыть (тап), изменить, удалить, учить (проект или колоду целиком). */
class ItemActions(
    val onOpen: (LibNode) -> Unit,
    val onEdit: (LibNode) -> Unit,
    val onDelete: (LibNode) -> Unit,
    val onStudy: (LibNode) -> Unit
)
