package com.dlstudio.app.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dlstudio.app.content.Topic
import com.dlstudio.app.progress.Lesson
import com.dlstudio.app.progress.TopicGraph
import com.dlstudio.app.progress.TopicProgress
import com.dlstudio.app.progress.Xp
import com.dlstudio.app.ui.theme.LocalStrings

/** Задание дня: значок, подпись и сколько сделано из скольких. */
data class Quest(val emoji: String, val title: String, val value: Int, val target: Int)

/** «Задания»: цель дня, ближайшая награда и следующий уровень — полосы заполняются, выполненное золотится. */
@Composable
fun QuestsCard(quests: List<Quest>) {
    val s = LocalStrings.current
    SettingCard(gap = 14.dp) {
        CardTitle(s.questsTitle, "${quests.count { it.value >= it.target }} / ${quests.size}")
        quests.forEach { QuestRow(it) }
    }
}

@Composable
private fun QuestRow(quest: Quest) {
    val colors = MaterialTheme.colorScheme
    val done = quest.value >= quest.target
    Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(12.dp), Alignment.CenterVertically) {
        Box(
            Modifier.size(44.dp).clip(CircleShape).background(colors.primaryContainer).then(if (done) Modifier.background(Brush.verticalGradient(GOLD)) else Modifier),
            Alignment.Center
        ) { Text(if (done) "✓" else quest.emoji, style = MaterialTheme.typography.titleLarge, color = if (done) Color(0xFF3A2400) else colors.onSurface) }
        Column(Modifier.weight(1f), Arrangement.spacedBy(6.dp)) {
            Text(quest.title, style = MaterialTheme.typography.titleSmall, maxLines = 2)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                ProgressBar(minOf(quest.value, quest.target) / quest.target.toFloat(), Modifier.weight(1f))
                Text("${minOf(quest.value, quest.target)}/${quest.target}", style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
            }
        }
    }
}

/**
 * «Ваш путь»: уроки цепочкой, у каждого кольцо «пройдено тем из всех». Урок, где остановились, пульсирует; тап открывает в нём ближайшую тему
 * (в уроке, где остановились, — ту самую).
 */
@Composable
fun PathCard(lessons: List<Lesson>, progress: Map<String, TopicProgress>, activeKey: String, resume: Topic?, onOpen: (String) -> Unit) {
    val s = LocalStrings.current
    val colors = MaterialTheme.colorScheme
    SettingCard(gap = 12.dp) {
        CardTitle(s.pathTitle)
        Row(Modifier.horizontalScroll(rememberScrollState())) {
            lessons.forEachIndexed { i, lesson ->
                val done = lesson.topics.count { Xp.complete(it, progress[it.id] ?: TopicProgress()) }
                if (i > 0) {
                    val walked = lessons[i - 1].topics.all { Xp.complete(it, progress[it.id] ?: TopicProgress()) }
                    Box(Modifier.padding(top = 31.dp).width(18.dp).height(3.dp).background(if (walked) colors.primary else colors.outline))
                }
                val target = resume?.takeIf { r -> lesson.topics.any { it.id == r.id } } ?: TopicGraph.open(lesson.topics, progress).firstOrNull() ?: lesson.topics.first()
                LessonBubble(lesson.group.ifBlank { s.noGroup }, done, lesson.topics.size, lesson.key == activeKey) { onOpen(target.id) }
            }
        }
    }
}

@Composable
private fun LessonBubble(title: String, done: Int, total: Int, active: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val pulse by rememberInfiniteTransition(label = "lesson").animateFloat(1f, if (active) 1.22f else 1f, infiniteRepeatable(tween(1000), RepeatMode.Reverse), label = "lesson")
    Column(Modifier.width(84.dp), Arrangement.spacedBy(6.dp), Alignment.CenterHorizontally) {
        Box(Modifier.size(64.dp), Alignment.Center) {
            if (active) Box(Modifier.size(64.dp).graphicsLayer { scaleX = pulse; scaleY = pulse }.background(colors.primary.copy(alpha = 0.18f), CircleShape))
            Ring(done / total.toFloat(), 58.dp, 6.dp, Modifier.clip(CircleShape).clickable(role = Role.Button, onClick = onClick)) {
                Text(if (done == total) "✓" else "$done/$total", style = MaterialTheme.typography.labelLarge, color = if (active || done == total) colors.primary else colors.onSurfaceVariant)
            }
        }
        Text(title, style = MaterialTheme.typography.labelMedium, color = if (active) colors.primary else colors.onSurfaceVariant, textAlign = TextAlign.Center, maxLines = 1)
    }
}
