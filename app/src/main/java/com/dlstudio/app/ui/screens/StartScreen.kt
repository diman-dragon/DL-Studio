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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.dlstudio.app.progress.Achievements
import com.dlstudio.app.progress.Motivation
import com.dlstudio.app.progress.TopicGraph
import com.dlstudio.app.progress.TopicProgress
import com.dlstudio.app.progress.Week
import com.dlstudio.app.progress.Xp
import com.dlstudio.app.ui.AppState
import com.dlstudio.app.ui.components.BadgeShelf
import com.dlstudio.app.ui.components.ContinueCard
import com.dlstudio.app.ui.components.GoalCard
import com.dlstudio.app.ui.components.GradientButton
import com.dlstudio.app.ui.components.RankCard
import com.dlstudio.app.ui.components.SettingCard
import com.dlstudio.app.ui.components.StatsRow
import com.dlstudio.app.ui.components.WeekCard
import com.dlstudio.app.ui.theme.LocalStrings
import java.util.Calendar

/**
 * Стартовый экран (самый левый): приветствие и фраза по тому, как человек занимался; цель дня с кольцом; «Продолжить»;
 * неделя столбиками; ранг; награды; итоги; мысль дня (тап — другая) и совет дня. Всё считается из действий пользователя.
 */
@Composable
fun StartScreen(state: AppState, onOpen: (String) -> Unit, onSettings: () -> Unit) {
    val s = LocalStrings.current
    val p = state.progress
    val today = Xp.dayNumber()
    val streak = Xp.currentStreak(p, today)
    val next = state.topics.groupBy { it.course }.values.firstNotNullOfOrNull { TopicGraph.open(it, p.topics).firstOrNull() }
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    var shift by rememberSaveable { mutableStateOf(0) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), Arrangement.spacedBy(14.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(s.greeting(Motivation.dayPart(hour)), style = MaterialTheme.typography.headlineMedium)
            Text(s.moodText(Motivation.mood(p, today), streak), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        GoalCard(Week.todayXp(p, today), state.dailyGoal, streak)
        when {
            next != null -> ContinueCard(next, Xp.stagesDone(next, p.topics[next.id] ?: TopicProgress())) { onOpen(next.id) }
            state.topics.isEmpty() -> GradientButton(s.openSettings, onSettings)
        }
        WeekCard(Week.xpByDay(p, today), state.dailyGoal, Week.weekday(today))
        RankCard(p.xp)
        val totals = Achievements.totals(p, state.topics, today)
        BadgeShelf(totals)
        StatsRow(totals)
        SettingCard(onClick = { shift += 1 }, gap = 6.dp) {
            Text(s.quoteTitle.uppercase(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Text(Motivation.ofDay(s.quotes, today, salt = shift), style = MaterialTheme.typography.titleMedium, fontStyle = FontStyle.Italic)
            Text(s.quoteMore, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        SettingCard(gap = 6.dp) {
            Text(s.tipTitle.uppercase(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Text(Motivation.ofDay(s.tips, today, salt = 3), style = MaterialTheme.typography.bodyLarge)
        }
    }
}
