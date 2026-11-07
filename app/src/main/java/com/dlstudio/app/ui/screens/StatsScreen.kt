package com.dlstudio.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dlstudio.app.library.LibraryViewModel
import com.dlstudio.app.study.DayMath
import com.dlstudio.app.study.Praise
import com.dlstudio.app.ui.components.MotivationBanner
import com.dlstudio.app.ui.components.ScreenHeader
import com.dlstudio.app.ui.components.StatsBarChart
import com.dlstudio.app.ui.components.StatsChartCard
import com.dlstudio.app.ui.components.StatsMetricRow
import com.dlstudio.app.ui.components.praiseEmoji
import com.dlstudio.app.ui.theme.LocalStrings
import com.dlstudio.app.ui.theme.tint
import com.dlstudio.app.study.Grade

/** Статистика: похвала по результатам, сводка по расписанию («к повтору» и «просрочено» — от начала дня), рекорды недели, активность и интервалы. */
@Composable
fun StatsScreen(vm: LibraryViewModel = viewModel()) {
    val s = LocalStrings.current
    val state by vm.state.collectAsStateWithLifecycle()
    val st = state.stats
    val p = MaterialTheme.colorScheme.primary
    val bad = Grade.AGAIN.tint()
    val week = st.weekReviews - st.prevWeekReviews
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), Arrangement.spacedBy(12.dp)) {
        ScreenHeader(s.tabStats)
        if (st.totalCards == 0) Text(s.noCards, color = MaterialTheme.colorScheme.onSurfaceVariant)
        else {
            if (st.studiedCards > 0) Praise.pick(st).let { (kind, n) -> MotivationBanner(s.praise(kind, n), praiseEmoji(kind)) }
            StatsMetricRow(listOf(Triple(s.statsDue, "${st.dueCards}", p), Triple(s.statsOverdue, "${st.overdueCards}", bad), Triple(s.statsNew, "${st.newCards}", p)))
            StatsMetricRow(listOf(Triple(s.statsToday, "${st.todayReviews}", p), Triple(s.statsStreak, "${st.streakDays} ${s.daysShort}", p), Triple(s.statsAccuracy, "${st.accuracy}%", p)))
            StatsMetricRow(listOf(Triple(s.statsAverage, "%.1f %s".format(st.averageFaceMs / 1000.0, s.secondsShort), p), Triple(s.statsStudied, "${st.studiedCards}", p), Triple(s.statsMature, "${st.matureCards}", p)))
            StatsMetricRow(listOf(
                Triple(s.statsBestStreak, "${st.bestStreak} ${s.daysShort}", p), Triple(s.statsWeek, "${st.weekReviews}", p),
                Triple(s.statsWeekTrend, if (week > 0) "▲ +$week" else if (week < 0) "▼ $week" else "0", if (week > 0) Grade.KNOW.tint() else MaterialTheme.colorScheme.onSurfaceVariant)
            ))
            StatsChartCard(s.statsReviewsTitle) { StatsBarChart(st.reviewsByDay, st.reviewsByDay.indices.map { s.weekdays[DayMath.weekday(state.today - st.reviewsByDay.lastIndex + it)] }, highlightLast = true) }
            StatsChartCard(s.statsIntervals) { StatsBarChart(st.intervalBuckets, listOf("1${s.daysShort}", "2–6${s.daysShort}", "7–29${s.daysShort}", "30${s.daysShort}+")) }
        }
    }
}
