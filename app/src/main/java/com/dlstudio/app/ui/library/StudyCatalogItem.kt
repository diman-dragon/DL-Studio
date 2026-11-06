package com.dlstudio.app.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.dlstudio.app.ui.components.CatalogMetrics
import com.dlstudio.app.ui.components.CatalogStat

@Composable
fun StudyCatalogItem(title: String, stat: CatalogStat, level: Int, tile: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    if (tile) Column(modifier.fillMaxWidth().height(184.dp).clip(RoundedCornerShape(24.dp)).background(MaterialTheme.colorScheme.surfaceVariant).clickable { onClick() }.padding(14.dp), Arrangement.SpaceBetween) { ItemBadge(level, 52.dp); Column { Text(title, style = MaterialTheme.typography.titleMedium); CatalogMetrics(stat) } }
    else Row(modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.surfaceVariant).clickable { onClick() }.padding(12.dp), Arrangement.spacedBy(14.dp), Alignment.CenterVertically) { ItemBadge(level, 48.dp); Column(Modifier.weight(1f)) { Text(title, style = MaterialTheme.typography.titleMedium); CatalogMetrics(stat) } }
}
