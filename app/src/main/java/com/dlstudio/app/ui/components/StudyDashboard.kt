package com.dlstudio.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dlstudio.app.library.LibraryState
import com.dlstudio.app.study.CardCounts
import com.dlstudio.app.study.DayMath
import com.dlstudio.app.study.Forecast
import com.dlstudio.app.study.Motivation
import com.dlstudio.app.study.SessionPicker
import com.dlstudio.app.study.StudyScope
import com.dlstudio.app.study.StudySession
import com.dlstudio.app.study.StudyViewModel
import com.dlstudio.app.ui.theme.LocalStrings

/** Дашборд «Учёба»: план на сегодня, проекты, мотивация, серия, итог вчерашнего дня и прогноз. */
@Composable
fun StudyDashboard(lib: LibraryState, sessionSize: Int, study: StudyViewModel, onSession: (StudySession) -> Unit) {
    val s = LocalStrings.current
    val all = remember(lib.items) { StudyScope.cards(lib.items, null, null) }
    val projects = remember(lib.items, lib.states, lib.today) {
        lib.items.map { p -> Triple(p.id, p.name, CardCounts.of(StudyScope.cards(lib.items, p.id, null), lib.states, lib.today)) }
    }
    val forecast = remember(lib.states, lib.today) { Forecast.next(lib.states.values, lib.today) }
    val (kind, n) = Motivation.pick(lib.stats)
    fun startAll(extra: Boolean) = study.begin(SessionPicker.pick(all, lib.states, lib.today, sessionSize, extra))?.let(onSession)
    fun startProject(id: String) = study.begin(SessionPicker.pick(StudyScope.cards(lib.items, id, null), lib.states, lib.today, sessionSize))?.let(onSession)
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), Arrangement.spacedBy(14.dp)) {
        ScreenHeader(s.tabStudy)
        StudyHero(CardCounts.of(all, lib.states, lib.today), sessionSize, lib.stats.todayReviews, { startAll(false) }, { startAll(true) }, { study.beginRandom(all)?.let(onSession) })
        MotivationBanner(s.motivation(kind, n), motivationEmoji(kind))
        if (projects.isNotEmpty()) Column(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
            Text(s.projectsTitle, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 4.dp))
            projects.forEach { (id, name, counts) -> StudyProjectCard(name, counts) { startProject(id) } }
        }
        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Max), Arrangement.spacedBy(12.dp)) {
            StreakCard(lib.stats, lib.today, Modifier.weight(1f))
            YesterdayCard(lib.stats, Modifier.weight(1f))
        }
        StatsChartCard(s.forecastTitle) { StatsBarChart(forecast, forecast.indices.map { s.weekdays[DayMath.weekday(lib.today + 1 + it)] }) }
    }
}
