package com.dlstudio.app.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dlstudio.app.study.CardCounts
import com.dlstudio.app.ui.components.CatalogMetrics
import com.dlstudio.app.ui.components.FitText
import com.dlstudio.app.ui.components.capFirst

/** Плитка или строка проекта/колоды на экране учёбы с общей сводкой карточек. */
@Composable
fun StudyCatalogItem(title: String, counts: CardCounts, level: Int, tile: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(if (tile) 24.dp else 20.dp)
    val base = modifier.fillMaxWidth().clip(shape).background(colors.surfaceVariant).border(1.dp, colors.outlineVariant, shape).clickable { onClick() }
    if (tile) Column(base.height(184.dp).padding(14.dp), Arrangement.SpaceBetween) {
        ItemBadge(level, 52.dp)
        Column { FitText(title.capFirst(), MaterialTheme.typography.titleLarge, Modifier.fillMaxWidth(), minSize = 12.sp, maxLines = 2); CatalogMetrics(counts) }
    } else Row(base.padding(12.dp), Arrangement.spacedBy(14.dp), Alignment.CenterVertically) {
        ItemBadge(level, 48.dp)
        Column(Modifier.weight(1f)) { FitText(title.capFirst(), MaterialTheme.typography.titleLarge, Modifier.fillMaxWidth(), minSize = 12.sp, maxLines = 2); CatalogMetrics(counts) }
    }
}
