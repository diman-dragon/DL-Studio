package com.dlstudio.app.ui.components

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dlstudio.app.study.CardCounts
import com.dlstudio.app.ui.theme.LocalAccent
import com.dlstudio.app.ui.theme.LocalStrings
import com.dlstudio.app.ui.theme.LocalStudyOptions
import com.dlstudio.app.ui.theme.gradientColors

/** Плитка «Сегодня»: счётчики (число «набегает»), кнопка старта и кольцо прогресса; когда план дня выполнен — конфетти. */
@Composable
fun StudyHero(counts: CardCounts, sessionSize: Int, doneToday: Int, onStart: () -> Unit, onExtra: () -> Unit, onPreview: () -> Unit) {
    val s = LocalStrings.current
    val planned = minOf(sessionSize, counts.due + counts.fresh)
    val progress = if (doneToday + planned == 0) 0f else doneToday.toFloat() / (doneToday + planned)
    val cs = MaterialTheme.colorScheme
    val shown by animateIntAsState(planned, tween(800), label = "planned")
    val complete = doneToday > 0 && planned == 0 && LocalStudyOptions.current.celebrate
    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(cs.surfaceContainerHigh).border(1.dp, cs.outlineVariant, RoundedCornerShape(28.dp))) {
        Row(Modifier.fillMaxWidth().padding(20.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Column(Modifier.weight(1f).padding(end = 16.dp), Arrangement.spacedBy(3.dp)) {
                Text(s.todayTitle, style = MaterialTheme.typography.labelLarge, color = cs.onSurfaceVariant)
                FitText(if (planned > 0 || counts.total == 0) "$shown" else s.nothingDue, MaterialTheme.typography.displaySmall, Modifier.fillMaxWidth(), cs.onSurface, 18.sp, 2)
                Text(
                    when { counts.total == 0 -> s.noCards; planned > 0 -> "${counts.due} ${s.due} · ${counts.fresh} ${s.freshWord}"; else -> s.nothingDueHint },
                    style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant
                )
                if (counts.total > 0) {
                    Spacer(Modifier.height(10.dp))
                    GradientButton(if (planned > 0) s.startStudy else s.practiceMore, if (planned > 0) onStart else onExtra)
                    TextButton(onPreview, Modifier.fillMaxWidth().height(48.dp)) { Text(s.previewCards) }
                }
            }
            Box(Modifier.clip(RoundedCornerShape(22.dp)).background(Brush.linearGradient(LocalAccent.current.gradientColors())).padding(6.dp)) {
                StudyRing(progress, "$doneToday", s.doneWord)
            }
        }
        if (complete) Confetti(Modifier.matchParentSize())
    }
}
