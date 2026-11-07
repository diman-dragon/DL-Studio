package com.dlstudio.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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

/** Дашборд «Учёба»: план на сегодня, мотивация, серия, итог вчерашнего дня, прогноз и проекты. */
@Composable
fun StudyDashboard(lib: LibraryState, sessionSize: Int, study: StudyViewModel, onSession: (StudySession) -> Unit) {
    val s = LocalStrings.current
    val all = remember(lib.items) { StudyScope.cards(lib.items, null, null) }
    val forecast = remember(lib.states, lib.today) { Forecast.next(lib.states.values, lib.today) }
    val (kind, n) = Motivation.pick(lib.stats)
    fun start(extra: Boolean) { study.begin(SessionPicker.pick(all, lib.states, lib.today, sessionSize, extra))?.let(onSession) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), Arrangement.spacedBy(14.dp)) {
        ScreenHeader(s.tabStudy)
        StudyHero(CardCounts.of(all, lib.states, lib.today), sessionSize, lib.stats.todayReviews, { start(false) }, { start(true) })
        MotivationBanner(s.motivation(kind, n))
        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Max), Arrangement.spacedBy(12.dp)) {
            StreakCard(lib.stats, lib.today, Modifier.weight(1f))
            YesterdayCard(lib.stats, Modifier.weight(1f))
        }
        StatsChartCard(s.forecastTitle) { StatsBarChart(forecast, forecast.indices.map { s.weekdays[DayMath.weekday(lib.today + 1 + it)] }) }
        DashProjects(lib) { study.openProject(it.id) }
    }
}
