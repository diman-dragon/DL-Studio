package com.dlstudio.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.dlstudio.app.study.CardCounts
import com.dlstudio.app.ui.theme.LocalStrings

data class StudyProjectSummary(val name: String, val counts: CardCounts)

@Composable
fun StudyHero(counts: CardCounts, sessionSize: Int, doneToday: Int, onStart: () -> Unit, onExtra: () -> Unit) =
    StudyHero(counts, emptyList(), sessionSize, doneToday, onStart, onExtra)

/** Плитка «Сегодня»: план сессии, кнопка старта и кольцо выполнения дня; когда всё сделано — предложение позаниматься сверх плана. */
@Composable
fun StudyHero(counts: CardCounts, projects: List<StudyProjectSummary>, sessionSize: Int, doneToday: Int, onStart: () -> Unit, onExtra: () -> Unit) {
    val s = LocalStrings.current
    val planned = minOf(sessionSize, counts.due + counts.fresh)
    val progress = if (doneToday + planned == 0) 0f else doneToday.toFloat() / (doneToday + planned)
    Column(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.18f), RoundedCornerShape(28.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Column(Modifier.weight(1f), Arrangement.spacedBy(4.dp)) {
                Text(s.todayTitle, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(if (planned > 0 || counts.total == 0) "$planned" else s.nothingDue, style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.primary)
                Text(
                    when { counts.total == 0 -> s.noCards; planned > 0 -> "${counts.due} ${s.due} · ${counts.fresh} ${s.freshWord}"; else -> s.nothingDueHint },
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (counts.total > 0) Text(
                    if (planned > 0) s.startStudy else s.practiceMore,
                    Modifier.padding(top = 8.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.primary).clickable(role = Role.Button, onClick = if (planned > 0) onStart else onExtra).padding(horizontal = 20.dp, vertical = 11.dp),
                    color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.titleMedium
                )
            }
            Box(Modifier.clip(RoundedCornerShape(24.dp)).background(MaterialTheme.colorScheme.primary).padding(4.dp)) {
                StudyRing(progress, "$doneToday", s.doneWord)
            }
        }
        if (projects.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                projects.forEach { project ->
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surface).padding(horizontal = 14.dp, vertical = 11.dp),
                        Arrangement.SpaceBetween, Alignment.CenterVertically
                    ) {
                        Text(project.name, Modifier.weight(1f), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                        Text("${project.counts.due} ${s.due} · ${project.counts.fresh} ${s.freshWord}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
