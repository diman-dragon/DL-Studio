package com.dlstudio.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dlstudio.app.library.LibraryViewModel
import com.dlstudio.app.ui.components.StatsBarChart
import com.dlstudio.app.ui.components.StatsMetric
import com.dlstudio.app.ui.theme.LocalStrings

@Composable
fun StatsScreen(vm: LibraryViewModel = viewModel()) {
    val s = LocalStrings.current; val state by vm.state.collectAsStateWithLifecycle(); val stat = state.stats; val hasReviews = stat.reviewsByDay.any { it > 0 }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(s.tabStats, style = MaterialTheme.typography.headlineMedium)
        if (stat.totalCards > 0) {
            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) { StatsMetric(s.statsCards, stat.totalCards.toString(), Modifier.weight(1f)); StatsMetric(s.statsDue, stat.dueCards.toString(), Modifier.weight(1f)); StatsMetric(s.statsOverdue, stat.overdueCards.toString(), Modifier.weight(1f)) }
            if (hasReviews) Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) { StatsMetric(s.statsToday, stat.todayReviews.toString(), Modifier.weight(1f)); StatsMetric(s.statsAverage, "${stat.averageFaceMs / 1000}s", Modifier.weight(1f)); StatsMetric(s.statsStreak, "${stat.streakDays}d", Modifier.weight(1f)) }
            if (hasReviews) Surface(Modifier.fillMaxWidth(), RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surfaceVariant) { Column(Modifier.padding(18.dp), Arrangement.spacedBy(12.dp)) { Text(s.statsReviewsChart, style = MaterialTheme.typography.titleMedium); StatsBarChart(stat.reviewsByDay, List(14) { if (it % 2 == 0) "${it + 1}" else "" }) } }
            if (hasReviews) Surface(Modifier.fillMaxWidth(), RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surfaceVariant) { Column(Modifier.padding(18.dp), Arrangement.spacedBy(12.dp)) { Text(s.statsIntervals, style = MaterialTheme.typography.titleMedium); StatsBarChart(stat.intervalBuckets, listOf("<1d", "1–7d", "7–30d", "30d+")) } }
        }
    }
}
