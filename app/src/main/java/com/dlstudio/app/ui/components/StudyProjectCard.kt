package com.dlstudio.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dlstudio.app.study.CardCounts
import com.dlstudio.app.ui.theme.LocalStrings

/**
 * Карточка проекта на экране «Учёба»: название, счётчики к повтору и новых,
 * и кнопка старта сессии по этому проекту.
 */
@Composable
fun StudyProjectCard(name: String, counts: CardCounts, onStart: () -> Unit) {
    val s = LocalStrings.current
    val colors = MaterialTheme.colorScheme
    val hasWork = counts.due > 0 || counts.fresh > 0
    Surface(
        Modifier.fillMaxWidth(),
        RoundedCornerShape(20.dp),
        color = colors.surfaceContainerLow,
        border = BorderStroke(1.dp, colors.outlineVariant)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            Arrangement.SpaceBetween,
            Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f).padding(end = 12.dp), Arrangement.spacedBy(3.dp)) {
                FitText(name.capFirst(), MaterialTheme.typography.titleLarge, Modifier.fillMaxWidth(), colors.onSurface, 12.sp, 2)
                if (counts.total == 0) {
                    Text(s.noCards, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                } else if (hasWork) {
                    Text(
                        "${counts.due} ${s.due} · ${counts.fresh} ${s.freshWord}",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                } else {
                    Text(s.nothingDue, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                }
            }
            if (hasWork) StudyPlayButton(onClick = onStart)
        }
    }
}
