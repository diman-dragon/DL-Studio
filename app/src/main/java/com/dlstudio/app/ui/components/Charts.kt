package com.dlstudio.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val BAR_SHARE = 0.62f
private const val GAP_DEGREES = 2f

/** Переключатель из нескольких вкладок в одну строку (например «Обзор» и «Статистика»). */
@Composable
fun SegmentedTabs(labels: List<String>, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(16.dp)
    Row(modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
        labels.forEachIndexed { i, label ->
            val on = i == selected
            Box(
                Modifier.weight(1f).clip(shape).background(if (on) colors.primary.copy(alpha = 0.22f) else colors.surfaceVariant)
                    .border(1.dp, if (on) colors.primary else colors.outlineVariant, shape).clickable(role = Role.Tab) { onSelect(i) }.padding(vertical = 12.dp),
                Alignment.Center
            ) { Text(label, style = MaterialTheme.typography.titleSmall, color = if (on) colors.primary else colors.onSurfaceVariant, fontWeight = FontWeight.SemiBold) }
        }
    }
}

/** Столбчатая диаграмма: на каждый день — столбец из сегментов (снизу вверх) с заданными цветами; под столбцами подписи. */
@Composable
fun StackedBars(values: List<List<Int>>, segmentColors: List<Color>, labels: List<String>, modifier: Modifier = Modifier, height: Dp = 140.dp) {
    val axis = MaterialTheme.colorScheme.outlineVariant
    if (values.isEmpty()) return
    val peak = maxOf(1, values.maxOfOrNull { row -> row.sumOf { maxOf(0, it) } } ?: 1)
    Column(modifier.fillMaxWidth(), Arrangement.spacedBy(4.dp)) {
        Canvas(Modifier.fillMaxWidth().height(height)) {
            val slot = size.width / values.size
            val barWidth = slot * BAR_SHARE
            drawLine(axis, Offset(0f, size.height), Offset(size.width, size.height), 2f)
            values.forEachIndexed { i, segments ->
                var top = size.height
                segments.forEachIndexed { k, amount ->
                    val h = maxOf(0, amount) / peak.toFloat() * size.height
                    drawRect(segmentColors.getOrElse(k) { axis }, Offset(i * slot + (slot - barWidth) / 2, top - h), Size(barWidth, h))
                    top -= h
                }
            }
        }
        Row(Modifier.fillMaxWidth()) {
            labels.forEach { Text(it, Modifier.weight(1f), MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp, textAlign = TextAlign.Center, maxLines = 1) }
        }
    }
}

/** Кольцевая диаграмма из частей (доля, цвет); пустая — серое кольцо. В центре — содержимое. */
@Composable
fun Donut(parts: List<Pair<Int, Color>>, diameter: Dp, thickness: Dp, modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val empty = MaterialTheme.colorScheme.outlineVariant
    val total = parts.sumOf { maxOf(0, it.first) }
    Box(modifier.size(diameter), Alignment.Center) {
        Canvas(Modifier.size(diameter)) {
            val stroke = Stroke(thickness.toPx(), cap = StrokeCap.Butt)
            val inset = thickness.toPx() / 2
            val box = Size(maxOf(0f, size.width - 2 * inset), maxOf(0f, size.height - 2 * inset))
            if (total <= 0) drawArc(empty, 0f, 360f, false, Offset(inset, inset), box, style = stroke)
            var start = -90f
            if (total > 0) parts.filter { it.first > 0 }.forEach { (amount, color) ->
                val sweep = amount * 360f / total
                drawArc(color, start, maxOf(0f, sweep - GAP_DEGREES), false, Offset(inset, inset), box, style = stroke)
                start += sweep
            }
        }
        content()
    }
}

/** Таблица: первая строка — шапка с заливкой, дальше строки через одну тоном светлее; первый столбец вдвое шире. */
@Composable
fun StatTable(header: List<String>, rows: List<List<String>>, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(16.dp)
    Column(modifier.fillMaxWidth().clip(shape).border(1.dp, colors.outlineVariant, shape)) {
        (listOf(header) + rows).forEachIndexed { i, row ->
            val fill = when { i == 0 -> colors.primaryContainer; i % 2 == 0 -> colors.surfaceContainerLow; else -> Color.Transparent }
            Row(Modifier.fillMaxWidth().background(fill).padding(horizontal = 12.dp, vertical = 10.dp), Arrangement.spacedBy(8.dp)) {
                row.forEachIndexed { k, cell ->
                    Text(cell, Modifier.weight(if (k == 0) 2f else 1f), style = MaterialTheme.typography.bodyMedium, fontWeight = if (i == 0) FontWeight.Bold else null, textAlign = if (k == 0) TextAlign.Start else TextAlign.End)
                }
            }
        }
    }
}

/** Плитка с крупным числом и подписью. */
@Composable
fun StatTile(value: String, label: String, modifier: Modifier = Modifier) {
    SettingCard(modifier, gap = 2.dp) {
        Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
