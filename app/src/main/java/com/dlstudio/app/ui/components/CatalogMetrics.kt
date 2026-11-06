package com.dlstudio.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dlstudio.app.study.CardCounts
import com.dlstudio.app.ui.theme.LocalStrings

/** Строки сводки под названием: всего; к повтору и новые; просроченные — те же определения, что в статистике. */
@Composable
fun CatalogMetrics(counts: CardCounts, modifier: Modifier = Modifier) {
    val s = LocalStrings.current
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    Column(modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text("${counts.total} ${s.statsCards.lowercase()}", style = MaterialTheme.typography.bodyMedium)
        Text("${counts.due} ${s.due} · ${counts.fresh} ${s.freshWord}", style = MaterialTheme.typography.bodySmall, color = muted)
        if (counts.overdue > 0) Text("${counts.overdue} ${s.overdue}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
    }
}
