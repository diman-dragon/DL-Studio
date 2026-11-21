package com.dlstudio.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dlstudio.app.R
import com.dlstudio.app.content.Topic
import com.dlstudio.app.progress.TopicProgress
import com.dlstudio.app.progress.Xp
import com.dlstudio.app.ui.AppState
import com.dlstudio.app.ui.components.AppIcon
import com.dlstudio.app.ui.components.GradientButton
import com.dlstudio.app.ui.components.Pill
import com.dlstudio.app.ui.components.Ring
import com.dlstudio.app.ui.components.ScreenHeader
import com.dlstudio.app.ui.theme.LocalStrings

/** Карта: темы выбранного курса по группам (уровням). Группа закрывается «печатью», когда пройдены все её темы. */
@Composable
fun MapScreen(state: AppState, onOpen: (String) -> Unit, onSettings: () -> Unit) {
    val s = LocalStrings.current
    val courses = state.topics.map { it.course }.distinct()
    var chosen by rememberSaveable { mutableStateOf("") }
    val course = if (chosen in courses) chosen else courses.firstOrNull().orEmpty()
    val groups = state.topics.filter { it.course == course }.groupBy { it.group }
    val today = Xp.dayNumber()

    LazyColumn(Modifier.fillMaxSize(), contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            ScreenHeader(s.tabMap)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Pill(s.levelTitle(Xp.level(state.progress.xp)), accent = true)
                Pill("${state.progress.xp} XP")
                Pill(s.streakDays(Xp.currentStreak(state.progress, today)))
            }
        }
        if (courses.size > 1) item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(courses) { name -> CourseChip(name, name == course) { chosen = name } }
            }
        }
        if (state.topics.isEmpty()) item { EmptyMap(onSettings) }
        groups.forEach { (group, topics) ->
            item(key = "g$course/$group") { GroupHeader(group.ifBlank { s.noGroup }, topics, state) }
            items(topics, key = { it.id }) { topic -> TopicCard(topic, state.progress.topics[topic.id] ?: TopicProgress()) { onOpen(topic.id) } }
        }
    }
}

@Composable
private fun CourseChip(name: String, selected: Boolean, onClick: () -> Unit) {
    Surface(Modifier.clickable(role = Role.Tab, onClick = onClick), RoundedCornerShape(16.dp), if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant) {
        Text(name, Modifier.padding(horizontal = 14.dp, vertical = 8.dp), if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelLarge, maxLines = 1)
    }
}

@Composable
private fun GroupHeader(group: String, topics: List<Topic>, state: AppState) {
    val s = LocalStrings.current
    val done = topics.count { Xp.complete(it, state.progress.topics[it.id] ?: TopicProgress()) }
    Row(Modifier.fillMaxWidth().padding(top = 8.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
        Text(group, style = MaterialTheme.typography.titleLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            if (done == topics.size) AppIcon(R.drawable.ic_check, MaterialTheme.colorScheme.primary, 20.dp)
            Text(s.groupProgress(done, topics.size), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun TopicCard(topic: Topic, progress: TopicProgress, onClick: () -> Unit) {
    val s = LocalStrings.current
    val colors = MaterialTheme.colorScheme
    val stages = Xp.stagesDone(topic, progress)
    Surface(Modifier.fillMaxWidth().clickable(role = Role.Button, onClick = onClick), RoundedCornerShape(24.dp), colors.surfaceVariant, border = BorderStroke(1.dp, colors.outlineVariant)) {
        Row(Modifier.padding(16.dp), Arrangement.spacedBy(14.dp), Alignment.CenterVertically) {
            Ring(stages / 3f, 52.dp, 5.dp) {
                if (stages == 3) AppIcon(R.drawable.ic_check, colors.primary, 26.dp) else Text("$stages/3", style = MaterialTheme.typography.labelLarge)
            }
            Column(Modifier.weight(1f), Arrangement.spacedBy(2.dp)) {
                Text(topic.title, style = MaterialTheme.typography.titleMedium, maxLines = 2)
                Text(s.cardsCount(topic.cards.size), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            }
            AppIcon(R.drawable.ic_play, colors.primary, 26.dp)
        }
    }
}

@Composable
private fun EmptyMap(onSettings: () -> Unit) {
    val s = LocalStrings.current
    Column(Modifier.fillMaxWidth().padding(vertical = 32.dp), Arrangement.spacedBy(12.dp), Alignment.CenterHorizontally) {
        Text(s.mapEmptyTitle, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Text(s.mapEmptyHint, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        GradientButton(s.openSettings, onSettings)
    }
}
