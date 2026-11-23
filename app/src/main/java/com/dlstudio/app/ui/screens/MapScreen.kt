package com.dlstudio.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dlstudio.app.content.Topic
import com.dlstudio.app.progress.Resume
import com.dlstudio.app.progress.TopicGraph
import com.dlstudio.app.progress.TopicProgress
import com.dlstudio.app.progress.Xp
import com.dlstudio.app.ui.AppState
import com.dlstudio.app.ui.components.ContinueCard
import com.dlstudio.app.ui.components.GradientButton
import com.dlstudio.app.ui.components.HEADER_H
import com.dlstudio.app.ui.components.LessonHeader
import com.dlstudio.app.ui.components.LessonLink
import com.dlstudio.app.ui.components.Pill
import com.dlstudio.app.ui.components.ROW
import com.dlstudio.app.ui.components.ScreenHeader
import com.dlstudio.app.ui.components.TRAIL_GAP
import com.dlstudio.app.ui.components.TopicGraphView
import com.dlstudio.app.ui.components.Trail
import com.dlstudio.app.ui.theme.LocalStrings

/** Раскрытые уроки хранятся строкой: ключи через этот разделитель (в ключе его быть не может). */
private const val SEP = "\u001F"

/**
 * Карта: сверху карточка «Продолжить», ниже уроки выбранного курса цепочкой. Урок свёрнут до шапки (кольцо, прогресс) или раскрыт в тропу тем
 * (или граф, если у тем есть предпосылки). При открытии раскрыт только урок, где остановились, и карта стоит на последней открытой теме.
 */
@Composable
fun MapScreen(state: AppState, onOpen: (String) -> Unit, onSettings: () -> Unit) {
    val s = LocalStrings.current
    val progress = state.progress.topics
    val courses = state.topics.map { it.course }.distinct()
    val focus = Resume.focus(state.topics, progress, state.lastTopic)
    var chosen by rememberSaveable { mutableStateOf("") }
    val course = if (chosen in courses) chosen else focus?.course ?: courses.firstOrNull().orEmpty()
    val inCourse = state.topics.filter { it.course == course }
    val lessons = Resume.lessons(inCourse)
    val last = Resume.last(inCourse, state.lastTopic)
    val focusHere = focus?.takeIf { it.course == course }
    val activeKey = Resume.active(lessons, focusHere)?.key
    val next = Resume.next(inCourse, progress, state.lastTopic)
    val today = Xp.dayNumber()

    var saved by rememberSaveable { mutableStateOf<String?>(null) }
    val expanded = saved?.split(SEP)?.filter { it.isNotEmpty() }?.toSet() ?: setOfNotNull(activeKey)
    val setExpanded = { keys: Set<String> -> saved = keys.joinToString(SEP) }
    val allOpen = lessons.isNotEmpty() && lessons.all { it.key in expanded }

    val lead = 1 + (if (next != null) 1 else 0) + (if (courses.size > 1) 1 else 0) + (if (lessons.size > 1) 1 else 0)
    val screen = LocalConfiguration.current.screenHeightDp.dp * 0.8f
    val density = LocalDensity.current
    val focusLesson = lessons.indexOfFirst { l -> focusHere != null && l.topics.any { it.id == focusHere.id } }
    val target = if (focusLesson < 0) 0 to 0 else focusScroll(lead + focusLesson, lessons[focusLesson].topics, focusHere!!.id, screen, density)
    val list = rememberLazyListState(target.first, target.second)
    val readyAtStart = remember { focusLesson >= 0 }
    LaunchedEffect(focusLesson >= 0) { if (focusLesson >= 0 && !readyAtStart) list.scrollToItem(target.first, target.second) }

    LazyColumn(Modifier.fillMaxSize(), state = list, contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            ScreenHeader(s.tabMap)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Pill(s.levelTitle(Xp.level(state.progress.xp)), accent = true)
                Pill("${state.progress.xp} XP")
                Pill(s.streakDays(Xp.currentStreak(state.progress, today)))
            }
        }
        if (next != null) item {
            ContinueCard(next, Xp.stagesDone(next, progress[next.id] ?: TopicProgress())) { onOpen(next.id) }
        }
        if (courses.size > 1) item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(courses) { name -> CourseChip(name, name == course) { chosen = name; saved = null } }
            }
        }
        if (state.topics.isEmpty()) item { EmptyMap(onSettings) }
        if (lessons.size > 1) item(key = "all$course") {
            Text(
                if (allOpen) s.collapseAll else s.expandAll, Modifier.fillMaxWidth().clickable { setExpanded(if (allOpen) emptySet() else lessons.map { it.key }.toSet()) },
                style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.End
            )
        }
        lessons.forEachIndexed { i, lesson ->
            item(key = lesson.key) {
                val open = lesson.key in expanded
                val done = lesson.topics.count { Xp.complete(it, progress[it.id] ?: TopicProgress()) }
                Column {
                    LessonHeader(lesson, i + 1, done, lesson.key == activeKey, last?.takeIf { l -> lesson.topics.any { it.id == l.id } }?.title, open) {
                        setExpanded(if (open) expanded - lesson.key else expanded + lesson.key)
                    }
                    AnimatedVisibility(open, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
                        Column {
                            Spacer(Modifier.height(TRAIL_GAP))
                            if (TopicGraph.isGraph(lesson.topics)) TopicGraphView(lesson.topics, progress, onOpen) else Trail(lesson.topics, progress, onOpen)
                        }
                    }
                    if (i < lessons.lastIndex) LessonLink(done == lesson.topics.size)
                }
            }
        }
    }
}

/**
 * Куда прокрутить, чтобы тема `topicId` раскрытого урока оказалась в верхней трети экрана: номер элемента списка (`item` — элемент урока) и сдвиг внутри него, в пикселях.
 * Строка темы — её место на тропе или слой в графе.
 */
private fun focusScroll(item: Int, topics: List<Topic>, topicId: String, viewport: Dp, density: Density): Pair<Int, Int> {
    val row = if (TopicGraph.isGraph(topics)) TopicGraph.layers(topics).getValue(topicId) else topics.indexOfFirst { it.id == topicId }
    val within = with(density) { (HEADER_H + TRAIL_GAP + ROW * row + ROW / 2 - viewport * 0.38f).roundToPx() }
    return item to within.coerceAtLeast(0)
}

@Composable
private fun CourseChip(name: String, selected: Boolean, onClick: () -> Unit) {
    Surface(Modifier.clickable(role = Role.Tab, onClick = onClick), RoundedCornerShape(16.dp), if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant) {
        Text(name, Modifier.padding(horizontal = 14.dp, vertical = 8.dp), if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelLarge, maxLines = 1)
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
