package com.dlstudio.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dlstudio.app.study.DayMath
import com.dlstudio.app.study.StudyStats
import com.dlstudio.app.ui.theme.LocalStrings

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
            if (stats.streakDays > 0) { Spacer(Modifier.weight(1f)); PulseEmoji("🔥", Modifier.padding(bottom = 4.dp), 30.sp) }
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
