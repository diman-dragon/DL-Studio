package com.dlstudio.app.ui.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dlstudio.app.library.LibNode
import com.dlstudio.app.study.CardCounts

/** Каталог проектов или колод для учёбы: сетка из двух колонок или список. Не прокручивается сам — лежит в прокручиваемом экране. */
@Composable
fun StudyCatalog(nodes: List<LibNode>, level: Int, grid: Boolean, countsFor: (LibNode) -> CardCounts, onNode: (LibNode) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (grid) nodes.chunked(2).forEach { row ->
            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(10.dp)) {
                row.forEach { StudyCatalogItem(it.name, countsFor(it), level, true, { onNode(it) }, Modifier.weight(1f)) }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        } else nodes.forEach { StudyCatalogItem(it.name, countsFor(it), level, false, { onNode(it) }) }
    }
}
