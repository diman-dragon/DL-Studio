package com.dlstudio.app.ui.library

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dlstudio.app.library.LibNode

@Composable
fun StudyCatalog(nodes: List<LibNode>, level: Int, allTitle: String?, allStat: com.dlstudio.app.ui.components.CatalogStat, grid: Boolean, statFor: (LibNode) -> com.dlstudio.app.ui.components.CatalogStat, onAll: () -> Unit, onNode: (LibNode) -> Unit, modifier: Modifier = Modifier) {
    val entries = if (allTitle == null) nodes.map { it to statFor(it) } else listOf(LibNode("*", allTitle) to allStat) + nodes.map { it to statFor(it) }; val content = modifier.verticalScroll(rememberScrollState()).padding(bottom = 100.dp)
    if (grid) Column(content, verticalArrangement = Arrangement.spacedBy(10.dp)) { entries.chunked(2).forEach { row -> Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(10.dp)) { row.forEach { (node, stat) -> StudyCatalogItem(node.name, stat, level, true, { if (node.id == "*") onAll() else onNode(node) }, Modifier.weight(1f)) }; if (row.size == 1) Spacer(Modifier.weight(1f)) } } }
    else Column(content, verticalArrangement = Arrangement.spacedBy(10.dp)) { entries.forEach { (node, stat) -> StudyCatalogItem(node.name, stat, level, false, { if (node.id == "*") onAll() else onNode(node) }, Modifier.fillMaxWidth()) } }
}
