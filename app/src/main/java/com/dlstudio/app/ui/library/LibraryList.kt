package com.dlstudio.app.ui.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.dlstudio.app.library.LibNode

/** Вид «список»: строки одна под другой. */
@Composable
fun LibraryList(nodes: List<LibNode>, level: Int, actions: ItemActions) {
    LazyColumn(
        contentPadding = PaddingValues(bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(nodes, key = { it.id }) { ItemRow(it, level, actions) }
    }
}
