package com.dlstudio.app.ui.library

import androidx.compose.runtime.Composable
import com.dlstudio.app.library.LibNode

/** Содержимое уровня: пусто → подсказка, иначе сетка или список по переключателю. */
@Composable
fun LibraryContent(nodes: List<LibNode>, level: Int, grid: Boolean, actions: ItemActions) {
    when {
        nodes.isEmpty() -> EmptyState(level)
        grid -> LibraryGrid(nodes, level, actions)
        else -> LibraryList(nodes, level, actions)
    }
}
