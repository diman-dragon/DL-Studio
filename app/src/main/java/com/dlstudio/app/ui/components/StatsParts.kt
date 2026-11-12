package com.dlstudio.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dlstudio.app.study.CardCounts
import com.dlstudio.app.study.DayMath
import com.dlstudio.app.study.StudyStats
import com.dlstudio.app.ui.theme.LocalAccent
import com.dlstudio.app.ui.theme.LocalStrings
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

/** Карточка под график: заголовок и содержимое. */
@Composable
fun StatsChartCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(Modifier.fillMaxWidth(), RoundedCornerShape(24.dp), colors.surfaceVariant, border = BorderStroke(1.dp, colors.outlineVariant)) {
        Column(Modifier.padding(18.dp), Arrangement.spacedBy(14.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

/** Плитка показателя: крупное значение (в цвете акцента или своём) и подпись. */
@Composable
fun StatsMetric(label: String, value: String, modifier: Modifier = Modifier, tint: Color = MaterialTheme.colorScheme.primary, onClick: () -> Unit = {}) {
    val colors = MaterialTheme.colorScheme
    Surface(modifier.clickable(onClick = onClick), RoundedCornerShape(20.dp), colors.surfaceVariant, border = BorderStroke(1.dp, colors.outlineVariant)) {
        Column(Modifier.padding(14.dp), Arrangement.spacedBy(4.dp)) {
            FitText(value, MaterialTheme.typography.headlineSmall, color = tint)
            Text(label, style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant, maxLines = 2)
        }
    }
}

/** Ряд равных плиток показателей: (подпись, значение, цвет значения). */
@Composable
fun StatsMetricRow(cells: List<Triple<String, String, Color>>, onClick: (Int) -> Unit = {}) {
    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Max), Arrangement.spacedBy(10.dp)) {
        cells.forEachIndexed { index, (label, value, tint) -> StatsMetric(label, value, Modifier.weight(1f), tint) { onClick(index) } }
    }
}

/** Серия дней с живым огоньком и точки активности за последнюю неделю (заполнена — в этот день были повторы). */
@Composable
fun StreakCard(stats: StudyStats, today: Long, modifier: Modifier = Modifier) {
    val s = LocalStrings.current
    val colors = MaterialTheme.colorScheme
    val week = stats.reviewsByDay.takeLast(7)
    DashCard(s.streakTitle, modifier) {
        Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(6.dp), Alignment.Bottom) {
            Text("${stats.streakDays}", style = MaterialTheme.typography.displaySmall, color = colors.primary)
            Text(s.daysShort, Modifier.padding(bottom = 6.dp), style = MaterialTheme.typography.titleMedium, color = colors.onSurfaceVariant)
            if (stats.streakDays > 0) { Spacer(Modifier.weight(1f)); PulseEmoji("🔥", Modifier.padding(bottom = 4.dp)) }
        }
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
            week.forEachIndexed { i, count ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Box(Modifier.size(12.dp).background(if (count > 0) colors.primary else colors.outline, CircleShape))
                    Text(s.weekdays[DayMath.weekday(today - week.lastIndex + i)].take(1), style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
                }
            }
        }
    }
}

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
