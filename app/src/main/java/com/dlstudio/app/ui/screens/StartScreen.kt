package com.dlstudio.app.ui.screens

import androidx.compose.animation.Crossfade
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
import com.dlstudio.app.progress.Resume
import com.dlstudio.app.progress.Review
import com.dlstudio.app.progress.TopicProgress
import com.dlstudio.app.progress.Week
import com.dlstudio.app.progress.Xp
import com.dlstudio.app.ui.AppState
import com.dlstudio.app.ui.components.BadgeShelf
import com.dlstudio.app.ui.components.ContinueCard
import com.dlstudio.app.ui.components.GoalCard
import com.dlstudio.app.ui.components.GradientButton
import com.dlstudio.app.ui.components.MascotBubble
import com.dlstudio.app.ui.components.PathCard
import com.dlstudio.app.ui.components.Quest
import com.dlstudio.app.ui.components.QuestsCard
import com.dlstudio.app.ui.components.RankCard
import com.dlstudio.app.ui.components.ReviewCard
import com.dlstudio.app.ui.components.Reveal
import com.dlstudio.app.ui.components.SettingCard
import com.dlstudio.app.ui.components.StatsRow
import com.dlstudio.app.ui.components.TopChips
import com.dlstudio.app.ui.components.WeekCard
import com.dlstudio.app.ui.theme.LocalStrings
import java.util.Calendar

/**
 * Стартовый экран (самый левый). Блоки по очереди выезжают при открытии: серия, XP и уровень; приветствие и талисман с репликой (тап — новая фраза);
 * цель дня; «Продолжить» с последней темы; задания; неделя кольцами; путь по урокам; ранг; награды; итоги; мысль дня (тап — другая) и совет дня.
 * Всё считается из действий пользователя.
 */
@Composable
fun StartScreen(state: AppState, onOpen: (String) -> Unit, onPractice: (String) -> Unit, onSettings: () -> Unit) {
    val s = LocalStrings.current
    val p = state.progress
    val today = Xp.dayNumber()
    val streak = Xp.currentStreak(p, today)
    val todayXp = Week.todayXp(p, today)
    val next = Resume.next(state.topics, p.topics, state.lastTopic)
    val review = Review.items(state.topics, p.topics)
    val lessons = Resume.lessons(state.topics)
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    var shift by rememberSaveable { mutableStateOf(0) }
    var cheer by rememberSaveable { mutableStateOf(-1) }
    val totals = Achievements.totals(p, state.topics, today)
    val quests = listOfNotNull(
        Quest("🎯", s.questGoal, todayXp, state.dailyGoal),
        Achievements.nextUp(totals)?.let { Quest(it.emoji, s.questBadge(s.badgeNames[it.ordinal]), Achievements.value(it, totals), it.target) },
        Quest("⭐", s.questLevel(Xp.level(p.xp) + 1), Xp.intoLevel(p.xp), Xp.LEVEL)
    )
    val said = if (cheer >= 0) s.cheers[cheer % s.cheers.size] else s.moodText(Motivation.mood(p, today), streak)

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), Arrangement.spacedBy(14.dp)) {
        Reveal(0) { TopChips(streak, p.xp, Xp.level(p.xp)) }
        Reveal(1) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(s.greeting(Motivation.dayPart(hour)), style = MaterialTheme.typography.headlineMedium)
                MascotBubble(said, todayXp >= state.dailyGoal) { cheer += 1 }
            }
        }
        Reveal(2) { GoalCard(todayXp, state.dailyGoal, streak) }
        Reveal(3) {
            when {
                next != null -> ContinueCard(next, Xp.stagesDone(next, p.topics[next.id] ?: TopicProgress())) { onOpen(next.id) }
                state.topics.isEmpty() -> GradientButton(s.openSettings, onSettings)
            }
        }
        if (review.isNotEmpty()) Reveal(4) { ReviewCard(review, onPractice) }
        Reveal(4) { QuestsCard(quests) }
        Reveal(5) { WeekCard(Week.xpByDay(p, today), state.dailyGoal, Week.weekday(today)) }
        if (lessons.isNotEmpty()) Reveal(6) {
            PathCard(lessons, p.topics, Resume.active(lessons, Resume.focus(state.topics, p.topics, state.lastTopic))?.key.orEmpty(), next, onOpen)
        }
        Reveal(7) { RankCard(p.xp) }
        Reveal(8) { BadgeShelf(totals) }
        Reveal(9) { StatsRow(totals) }
        Reveal(10) {
            SettingCard(onClick = { shift += 1 }, gap = 6.dp) {
                Text(s.quoteTitle.uppercase(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Crossfade(Motivation.ofDay(s.quotes, today, salt = shift), label = "quote") { quote ->
                    Text(quote, style = MaterialTheme.typography.titleMedium, fontStyle = FontStyle.Italic)
                }
                Text(s.quoteMore, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Reveal(11) {
            SettingCard(gap = 6.dp) {
                Text(s.tipTitle.uppercase(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Text(Motivation.ofDay(s.tips, today, salt = 3), style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
