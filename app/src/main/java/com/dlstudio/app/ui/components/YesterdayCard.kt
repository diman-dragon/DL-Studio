package com.dlstudio.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dlstudio.app.study.Grade
import com.dlstudio.app.study.StudyStats
import com.dlstudio.app.ui.theme.LocalStrings
import com.dlstudio.app.ui.theme.tint

/** Итог вчерашнего дня: сколько повторов и точность; без занятий — спокойное сообщение. */
@Composable
fun YesterdayCard(stats: StudyStats, modifier: Modifier = Modifier) {
    val s = LocalStrings.current
    DashCard(s.yesterdayTitle, modifier) {
        if (stats.yesterdayReviews == 0) Text(s.yesterdayEmpty, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        else Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("${stats.yesterdayReviews}", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.primary)
            Text(s.reviewsCount(stats.yesterdayReviews).substringAfter(' '), style = MaterialTheme.typography.bodyMedium)
            Text("${stats.yesterdayAccuracy}% ${s.statsAccuracy.lowercase()}", style = MaterialTheme.typography.labelLarge, color = Grade.KNOW.tint())
        }
    }
}
