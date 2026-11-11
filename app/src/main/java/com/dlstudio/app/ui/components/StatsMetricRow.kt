package com.dlstudio.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Ряд равных плиток показателей: (подпись, значение, цвет значения). */
@Composable
fun StatsMetricRow(cells: List<Triple<String, String, Color>>, onClick: (Int) -> Unit = {}) {
    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Max), Arrangement.spacedBy(10.dp)) {
        cells.forEachIndexed { index, (label, value, tint) -> StatsMetric(label, value, Modifier.weight(1f), tint) { onClick(index) } }
    }
}
