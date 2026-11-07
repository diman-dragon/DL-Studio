package com.dlstudio.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.dlstudio.app.study.CardCounts
import com.dlstudio.app.ui.theme.LocalAccent
import com.dlstudio.app.ui.theme.LocalStrings
import com.dlstudio.app.ui.theme.gradientColors

/** Героическая плитка дня: счётчики, кнопка старта и кольцо прогресса. Список проектов убран — он в библиотеке. */
@Composable
fun StudyHero(counts: CardCounts, sessionSize: Int, doneToday: Int, onStart: () -> Unit, onExtra: () -> Unit) {
    val s = LocalStrings.current
    val accent = LocalAccent.current
    val planned = minOf(sessionSize, counts.due + counts.fresh)
    val progress = if (doneToday + planned == 0) 0f else doneToday.toFloat() / (doneToday + planned)
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(28.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Column(Modifier.weight(1f).padding(end = 16.dp), Arrangement.spacedBy(4.dp)) {
                Text(s.todayTitle, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    if (planned > 0 || counts.total == 0) "$planned" else s.nothingDue,
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    when {
                        counts.total == 0 -> s.noCards
                        planned > 0 -> "${counts.due} ${s.due} · ${counts.fresh} ${s.freshWord}"
                        else -> s.nothingDueHint
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (counts.total > 0) {
                    Spacer(Modifier.height(4.dp))
                    GradientButton(
                        label = if (planned > 0) s.startStudy else s.practiceMore,
                        onClick = if (planned > 0) onStart else onExtra
                    )
                }
            }
            // Кольцо прогресса на мягком акцентном фоне, а не на сплошном primary
            Box(
                Modifier
                    .clip(RoundedCornerShape(22.dp))
                    .background(Brush.linearGradient(accent.gradientColors()))
                    .padding(6.dp)
            ) {
                StudyRing(progress, "$doneToday", s.doneWord)
            }
        }
    }
}
