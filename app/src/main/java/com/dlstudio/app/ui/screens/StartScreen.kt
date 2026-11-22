package com.dlstudio.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.dlstudio.app.progress.Motivation
import com.dlstudio.app.progress.TopicGraph
import com.dlstudio.app.progress.TopicProgress
import com.dlstudio.app.progress.Xp
import com.dlstudio.app.ui.AppState
import com.dlstudio.app.ui.components.ContinueCard
import com.dlstudio.app.ui.components.GradientButton
import com.dlstudio.app.ui.components.Pill
import com.dlstudio.app.ui.components.ScreenHeader
import com.dlstudio.app.ui.components.SettingCard
import com.dlstudio.app.ui.theme.LocalStrings
import java.util.Calendar

/**
 * Стартовый экран (самый левый): приветствие по времени суток, фраза по тому, как человек занимался (серия, давно не был и т. п.),
 * кнопка «Продолжить», мысль и совет дня. Всё выводится из поведения пользователя — никаких самооценок.
 */
@Composable
fun StartScreen(state: AppState, onOpen: (String) -> Unit, onSettings: () -> Unit) {
    val s = LocalStrings.current
    val today = Xp.dayNumber()
    val mood = Motivation.mood(state.progress, today)
    val streak = Xp.currentStreak(state.progress, today)
    val next = state.topics.groupBy { it.course }.values.firstNotNullOfOrNull { TopicGraph.open(it, state.progress.topics).firstOrNull() }
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val xp = state.progress.xp

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), Arrangement.spacedBy(12.dp)) {
        ScreenHeader(s.greeting(Motivation.dayPart(hour)))
        Text(s.moodText(mood, streak), style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Pill(s.levelTitle(Xp.level(xp)), accent = true)
            Pill("$xp XP")
            Pill(s.streakDays(streak))
        }
        when {
            next != null -> ContinueCard(next, Xp.stagesDone(next, state.progress.topics[next.id] ?: TopicProgress())) { onOpen(next.id) }
            state.topics.isEmpty() -> GradientButton(s.openSettings, onSettings)
        }
        SettingCard(gap = 6.dp) {
            Text(s.quoteTitle.uppercase(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Text(Motivation.ofDay(s.quotes, today), style = MaterialTheme.typography.titleMedium, fontStyle = FontStyle.Italic)
        }
        SettingCard(gap = 6.dp) {
            Text(s.tipTitle.uppercase(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Text(Motivation.ofDay(s.tips, today, salt = 3), style = MaterialTheme.typography.bodyLarge)
        }
    }
}
