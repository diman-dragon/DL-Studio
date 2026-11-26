package com.dlstudio.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dlstudio.app.content.QuizKind
import com.dlstudio.app.i18n.Strings
import com.dlstudio.app.progress.Counts
import com.dlstudio.app.progress.Stats
import com.dlstudio.app.progress.Xp
import com.dlstudio.app.ui.AppState
import com.dlstudio.app.ui.components.Donut
import com.dlstudio.app.ui.components.ProgressBar
import com.dlstudio.app.ui.components.SettingCard
import com.dlstudio.app.ui.components.SettingsSection
import com.dlstudio.app.ui.components.StackedBars
import com.dlstudio.app.ui.components.StatTable
import com.dlstudio.app.ui.components.StatTile
import com.dlstudio.app.ui.theme.LocalStrings
import java.util.GregorianCalendar
import java.util.TimeZone

private val OK_COLOR = Color(0xFF34D399)
private val WRONG_COLOR = Color(0xFFFF6B6B)
private const val CHART_DAYS = 14
private const val MS_PER_DAY = 86_400_000L

private fun kindName(s: Strings, kind: String) = when (kind) {
    QuizKind.CHOICE.name -> s.kindChoice
    QuizKind.LISTEN.name -> s.kindListen
    QuizKind.SPEAK.name -> s.kindSpeak
    QuizKind.BANK.name -> s.kindBank
    QuizKind.FILL.name -> s.kindFill
    QuizKind.GAP.name -> s.kindGap
    QuizKind.FIND.name -> s.kindFind
    else -> s.kindMatch
}

/** Число месяца у номера дня (см. Xp.dayNumber) — подпись под столбцом. */
private fun dayOfMonth(day: Long): String = GregorianCalendar(TimeZone.getTimeZone("UTC")).apply { timeInMillis = day * MS_PER_DAY }.get(java.util.Calendar.DAY_OF_MONTH).toString()

private fun row(name: String, c: Counts) = listOf(name, "${c.ok}", "${c.wrong}", "${c.percent}%")

/** Статистика: итоги, кольцо верных и неверных ответов, графики по дням, таблицы по видам заданий и по темам, произношение. Всё из действий пользователя. */
@Composable
fun StatsContent(state: AppState) {
    val s = LocalStrings.current
    val stats = state.progress.stats
    val answers = stats.answers
    if (answers.total + stats.cards + stats.pages + stats.listens + stats.pron.tries == 0) {
        Text(s.statsEmpty, Modifier.padding(vertical = 24.dp), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }
    val today = Xp.dayNumber()
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Totals(stats, answers)
        AnswersDonut(answers)
        Charts(state, stats, today)
        Tables(state, stats)
        Pronunciation(stats)
    }
}

@Composable
private fun Totals(stats: Stats, answers: Counts) {
    val s = LocalStrings.current
    Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(12.dp)) {
        StatTile("${answers.ok}", s.statsRight, Modifier.weight(1f))
        StatTile("${answers.wrong}", s.statsWrong, Modifier.weight(1f))
    }
    Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(12.dp)) {
        StatTile("${answers.percent}%", s.statsAccuracy, Modifier.weight(1f))
        StatTile("${stats.cards}", s.statsCards, Modifier.weight(1f))
    }
    Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(12.dp)) {
        StatTile("${stats.pages}", s.statsPages, Modifier.weight(1f))
        StatTile("${stats.listens}", s.statsListens, Modifier.weight(1f))
    }
}

@Composable
private fun AnswersDonut(answers: Counts) {
    val s = LocalStrings.current
    SettingsSection(s.statsAnswers) {
        SettingCard {
            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(20.dp), Alignment.CenterVertically) {
                Donut(listOf(answers.ok to OK_COLOR, answers.wrong to WRONG_COLOR), 132.dp, 16.dp) {
                    Text("${answers.percent}%", style = MaterialTheme.typography.headlineSmall)
                }
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("● ${s.statsRight}: ${answers.ok}", color = OK_COLOR, style = MaterialTheme.typography.titleMedium)
                    Text("● ${s.statsWrong}: ${answers.wrong}", color = WRONG_COLOR, style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
private fun Charts(state: AppState, stats: Stats, today: Long) {
    val s = LocalStrings.current
    val days = List(CHART_DAYS) { today - (CHART_DAYS - 1 - it) }
    val labels = days.map(::dayOfMonth)
    SettingsSection(s.statsXpDays) {
        SettingCard { StackedBars(days.map { listOf(state.progress.history[it] ?: 0) }, listOf(MaterialTheme.colorScheme.primary), labels) }
    }
    SettingsSection(s.statsAnswerDays) {
        SettingCard {
            StackedBars(stats.lastDays(today, CHART_DAYS).map { listOf(it.answers.ok, it.answers.wrong) }, listOf(OK_COLOR, WRONG_COLOR), labels)
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("● ${s.statsRight}", color = OK_COLOR, style = MaterialTheme.typography.bodySmall)
                Text("● ${s.statsWrong}", color = WRONG_COLOR, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
    SettingsSection(s.statsCardDays) {
        SettingCard { StackedBars(stats.lastDays(today, CHART_DAYS).map { listOf(it.cards) }, listOf(MaterialTheme.colorScheme.tertiary), labels) }
    }
}

@Composable
private fun Tables(state: AppState, stats: Stats) {
    val s = LocalStrings.current
    val header = listOf("", s.statsRight, s.statsWrong, "%")
    val kinds = QuizKind.values().map { it.name }.filter { (stats.byKind[it]?.total ?: 0) > 0 }
    if (kinds.isNotEmpty()) SettingsSection(s.statsByKind) { StatTable(header, kinds.map { row(kindName(s, it), stats.byKind.getValue(it)) }) }
    val titles = state.topics.associate { it.id to it.title }
    val topics = stats.byTopic.filter { it.value.total > 0 && it.key in titles }.entries.sortedByDescending { it.value.total }
    if (topics.isNotEmpty()) SettingsSection(s.statsByTopic) { StatTable(header, topics.map { row(titles.getValue(it.key), it.value) }) }
}

@Composable
private fun Pronunciation(stats: Stats) {
    val s = LocalStrings.current
    val p = stats.pron
    if (p.tries == 0) return
    SettingsSection(s.statsPron) {
        SettingCard(gap = 10.dp) {
            StatTable(listOf("", ""), listOf(listOf(s.statsTries, "${p.tries}"), listOf(s.statsAvg, "${p.average}%"), listOf(s.statsBest, "${p.best}%")))
            listOf(s.statsGreat to p.great, s.statsGood to p.good, s.statsRetry to p.retry).forEach { (name, count) ->
                Text("$name: $count", style = MaterialTheme.typography.bodyMedium)
                ProgressBar(count / p.tries.toFloat())
            }
        }
    }
}
