package com.dlstudio.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dlstudio.app.progress.Achievements
import com.dlstudio.app.progress.Badge
import com.dlstudio.app.progress.Totals
import com.dlstudio.app.progress.Xp
import com.dlstudio.app.progress.rankIndex
import com.dlstudio.app.ui.theme.LocalAccent
import com.dlstudio.app.ui.theme.LocalStrings
import com.dlstudio.app.ui.theme.gradientColors

private val GOLD = listOf(Color(0xFFFFD166), Color(0xFFFF9A3D))

/** Заголовок карточки: мелкие акцентные капсы слева и необязательная подпись справа. */
@Composable
private fun CardTitle(title: String, note: String? = null) {
    val colors = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
        Text(title.uppercase(), style = MaterialTheme.typography.labelMedium, color = colors.primary)
        if (note != null) Text(note, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
    }
}

/** Цель дня: кольцо с процентом, сколько XP набрано сегодня и серия дней. Выполнена — праздничный вид. */
@Composable
fun GoalCard(todayXp: Int, goal: Int, streak: Int) {
    val s = LocalStrings.current
    val done = todayXp >= goal
    SettingCard(gap = 10.dp) {
        Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(18.dp), Alignment.CenterVertically) {
            Ring(todayXp / goal.toFloat(), 104.dp, 11.dp) {
                Text(if (done) "🎉" else "${todayXp * 100 / goal}%", style = MaterialTheme.typography.titleLarge)
            }
            Column(Modifier.weight(1f), Arrangement.spacedBy(4.dp)) {
                Text(s.goalTitle.uppercase(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Text(if (done) s.goalDone else s.goalProgress(todayXp, goal), style = MaterialTheme.typography.titleMedium)
                if (streak > 0) Text("🔥 ${s.streakDays(streak)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/** Неделя столбиками: XP по дням с понедельника. Столбик цвета акцента, а в день, когда цель выполнена, — золотой. Сегодня подписан акцентом. */
@Composable
fun WeekCard(xps: List<Int>, goal: Int, today: Int) {
    val s = LocalStrings.current
    val colors = MaterialTheme.colorScheme
    val accent = LocalAccent.current.gradientColors()
    val scale = maxOf(goal, xps.max()).toFloat()
    SettingCard(gap = 12.dp) {
        CardTitle(s.weekTitle, s.weekTotal(xps.sum()))
        Row(Modifier.fillMaxWidth().height(104.dp), Arrangement.SpaceEvenly) {
            xps.forEachIndexed { day, xp ->
                val fill = when {
                    xp >= goal -> GOLD
                    xp > 0 -> accent
                    else -> listOf(colors.outline, colors.outline)
                }
                Column(Modifier.fillMaxHeight().width(30.dp), Arrangement.spacedBy(6.dp), Alignment.CenterHorizontally) {
                    Box(Modifier.weight(1f).fillMaxWidth(), Alignment.BottomCenter) {
                        Box(Modifier.fillMaxWidth().fillMaxHeight(maxOf(0.08f, xp / scale)).clip(RoundedCornerShape(8.dp)).background(Brush.verticalGradient(fill)))
                    }
                    Text(
                        s.weekdays[day], style = MaterialTheme.typography.labelMedium, maxLines = 1,
                        color = if (day == today) colors.primary else colors.onSurfaceVariant,
                        fontWeight = if (day == today) FontWeight.Bold else null
                    )
                }
            }
        }
    }
}

/** Ранг по уровню: звание, уровень, XP и полоса до следующего уровня. */
@Composable
fun RankCard(xp: Int) {
    val s = LocalStrings.current
    val level = Xp.level(xp)
    SettingCard(gap = 10.dp) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(s.ranks[rankIndex(level, s.ranks.size)], style = MaterialTheme.typography.titleLarge)
                Text(s.levelTitle(level), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("$xp XP", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        }
        ProgressBar(Xp.intoLevel(xp) / Xp.LEVEL.toFloat())
        Text(s.xpToNext(Xp.LEVEL - Xp.intoLevel(xp)), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** Награды лентой: полученные цветные, остальные тусклые со счётчиком; тап по награде — подсказка, что для неё сделать. */
@Composable
fun BadgeShelf(totals: Totals) {
    val s = LocalStrings.current
    var open by remember { mutableStateOf<Badge?>(null) }
    val all = Badge.values()
    Column(Modifier.fillMaxWidth(), Arrangement.spacedBy(10.dp)) {
        Box(Modifier.padding(horizontal = 6.dp)) { CardTitle(s.badgesTitle, s.badgesCount(all.count { Achievements.unlocked(it, totals) }, all.size)) }
        Row(Modifier.horizontalScroll(rememberScrollState()), Arrangement.spacedBy(10.dp)) {
            all.forEach { BadgeTile(it, totals) { open = it } }
        }
        Achievements.nextUp(totals)?.let { next ->
            Text(
                s.nextBadge(s.badgeNames[next.ordinal], Achievements.value(next, totals), next.target), Modifier.padding(horizontal = 6.dp),
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    open?.let { badge ->
        DlModal.Shell(s.badgeNames[badge.ordinal], { open = null }) {
            Text("${badge.emoji}  ${s.badgeHints[badge.ordinal]}", style = MaterialTheme.typography.bodyLarge)
            DlModal.Action(s.finish, { open = null }, Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun BadgeTile(badge: Badge, totals: Totals, onClick: () -> Unit) {
    val s = LocalStrings.current
    val colors = MaterialTheme.colorScheme
    val on = Achievements.unlocked(badge, totals)
    val shape = RoundedCornerShape(20.dp)
    Column(
        Modifier.width(100.dp).clip(shape).background(if (on) colors.primaryContainer else colors.surfaceVariant)
            .border(1.dp, if (on) colors.primary else colors.outlineVariant, shape).clickable(role = Role.Button, onClick = onClick).padding(10.dp),
        Arrangement.spacedBy(4.dp), Alignment.CenterHorizontally
    ) {
        Text(badge.emoji, Modifier.alpha(if (on) 1f else 0.35f), style = MaterialTheme.typography.headlineMedium)
        Text(
            s.badgeNames[badge.ordinal], Modifier.height(32.dp), if (on) colors.onSurface else colors.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center, maxLines = 2
        )
        Text(
            if (on) "✓" else "${minOf(Achievements.value(badge, totals), badge.target)}/${badge.target}", style = MaterialTheme.typography.labelSmall,
            color = if (on) colors.primary else colors.onSurfaceVariant
        )
    }
}

/** Три итога крупными числами: темы, карточки, верные ответы. */
@Composable
fun StatsRow(totals: Totals) {
    val s = LocalStrings.current
    Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(10.dp)) {
        StatTile("${totals.topics}", s.statTopics, Modifier.weight(1f))
        StatTile("${totals.cards}", s.statCards, Modifier.weight(1f))
        StatTile("${totals.answers}", s.statAnswers, Modifier.weight(1f))
    }
}

@Composable
private fun StatTile(value: String, label: String, modifier: Modifier) {
    val colors = MaterialTheme.colorScheme
    Surface(modifier, RoundedCornerShape(20.dp), colors.surfaceVariant, colors.onSurface, border = androidx.compose.foundation.BorderStroke(1.dp, colors.outlineVariant)) {
        Column(Modifier.padding(horizontal = 8.dp, vertical = 12.dp), Arrangement.spacedBy(2.dp), Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.headlineSmall, color = colors.primary)
            Text(label, style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant, textAlign = TextAlign.Center, maxLines = 2)
        }
    }
}
