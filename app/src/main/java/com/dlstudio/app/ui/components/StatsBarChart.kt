package com.dlstudio.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dlstudio.app.ui.theme.LocalAccent
import com.dlstudio.app.ui.theme.gradientColors

/** Столбчатая диаграмма: градиент акцента, значение над столбцом; `highlightLast` выделяет сегодняшний день. */
@Composable
fun StatsBarChart(values: List<Int>, labels: List<String>, modifier: Modifier = Modifier, highlightLast: Boolean = false) {
    val max = values.maxOrNull()?.coerceAtLeast(1) ?: 1
    val colors = MaterialTheme.colorScheme
    val gradient = Brush.verticalGradient(LocalAccent.current.gradientColors())
    Column(modifier, Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth().height(132.dp), Arrangement.spacedBy(4.dp), Alignment.Bottom) {
            values.forEachIndexed { i, value ->
                Column(Modifier.weight(1f).fillMaxHeight(), Arrangement.Bottom, Alignment.CenterHorizontally) {
                    if (value > 0) Text("$value", style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant, maxLines = 1, softWrap = false)
                    val dim = highlightLast && i != values.lastIndex
                    Column(Modifier.fillMaxWidth().height(maxOf(3.dp, 96.dp * (value.toFloat() / max))).background(gradient, RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp), if (dim) 0.55f else 1f)) {}
                }
            }
        }
        Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(4.dp)) {
            labels.forEach { Text(it, Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, maxLines = 1, softWrap = false, textAlign = TextAlign.Center, color = colors.onSurfaceVariant) }
        }
    }
}
