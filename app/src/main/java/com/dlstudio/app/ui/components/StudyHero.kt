package com.dlstudio.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.dlstudio.app.study.CardCounts
import com.dlstudio.app.ui.theme.LocalAccent
import com.dlstudio.app.ui.theme.LocalStrings
import com.dlstudio.app.ui.theme.gradientColors

/** Плитка «Сегодня»: план сессии, кнопка старта и кольцо выполнения дня; когда всё сделано — предложение позаниматься сверх плана. */
@Composable
fun StudyHero(counts: CardCounts, sessionSize: Int, doneToday: Int, onStart: () -> Unit, onExtra: () -> Unit) {
    val s = LocalStrings.current
    val planned = minOf(sessionSize, counts.due + counts.fresh)
    val progress = if (doneToday + planned == 0) 0f else doneToday.toFloat() / (doneToday + planned)
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(Brush.linearGradient(LocalAccent.current.gradientColors())).padding(22.dp),
        Arrangement.SpaceBetween, Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f), Arrangement.spacedBy(4.dp)) {
            Text(s.todayTitle, style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.8f))
            Text(if (planned > 0 || counts.total == 0) "$planned" else s.nothingDue, style = MaterialTheme.typography.displaySmall, color = Color.White)
            Text(
                when { counts.total == 0 -> s.noCards; planned > 0 -> "${counts.due} ${s.due} · ${counts.fresh} ${s.freshWord}"; else -> s.nothingDueHint },
                style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.85f)
            )
            if (counts.total > 0) Text(
                if (planned > 0) s.startStudy else s.practiceMore,
                Modifier.padding(top = 12.dp).clip(RoundedCornerShape(16.dp)).background(Color.White).clickable(role = Role.Button, onClick = if (planned > 0) onStart else onExtra).padding(horizontal = 20.dp, vertical = 11.dp),
                color = Color(LocalAccent.current.deep), style = MaterialTheme.typography.titleMedium
            )
        }
        StudyRing(progress, "$doneToday", s.doneWord, Modifier.padding(start = 12.dp))
    }
}
