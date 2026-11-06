package com.dlstudio.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dlstudio.app.ui.theme.LocalStrings

@Composable
fun CatalogMetrics(stat: CatalogStat, modifier: Modifier = Modifier) {
    val s = LocalStrings.current
    Column(modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text("${stat.total} ${s.statsCards.lowercase()}", style = MaterialTheme.typography.bodyMedium)
        Text("${stat.due} ${s.due}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("${stat.overdue} ${s.overdue}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
