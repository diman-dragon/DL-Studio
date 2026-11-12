package com.dlstudio.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dlstudio.app.R
import com.dlstudio.app.library.LibNode
import com.dlstudio.app.library.LibraryState
import com.dlstudio.app.library.LibraryViewModel
import com.dlstudio.app.study.CardCounts
import com.dlstudio.app.study.DayMath
import com.dlstudio.app.study.Forecast
import com.dlstudio.app.study.Grade
import com.dlstudio.app.study.Motivation
import com.dlstudio.app.study.MotivationKind
import com.dlstudio.app.study.SessionPicker
import com.dlstudio.app.study.StudyScope
import com.dlstudio.app.study.StudySession
import com.dlstudio.app.study.StudyStats
import com.dlstudio.app.study.StudyUi
import com.dlstudio.app.study.StudyViewModel
import com.dlstudio.app.ui.library.LibraryHeader
import com.dlstudio.app.ui.library.StudyCatalog
import com.dlstudio.app.ui.library.ViewToggle
import com.dlstudio.app.ui.theme.LocalAccent
import com.dlstudio.app.ui.theme.LocalStrings
import com.dlstudio.app.ui.theme.LocalStudyOptions
import com.dlstudio.app.ui.theme.gradientColors
import com.dlstudio.app.ui.theme.tint
import kotlinx.coroutines.delay

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

/** Плитка «Сегодня»: счётчики (число «набегает»), кнопка старта и кольцо прогресса; когда план дня выполнен — конфетти. */
@Composable
fun StudyHero(counts: CardCounts, sessionSize: Int, doneToday: Int, onStart: () -> Unit, onExtra: () -> Unit, onPreview: () -> Unit) {
    val s = LocalStrings.current
    val planned = minOf(sessionSize, counts.due + counts.fresh)
    val progress = if (doneToday + planned == 0) 0f else doneToday.toFloat() / (doneToday + planned)
    val cs = MaterialTheme.colorScheme
    val shown by animateIntAsState(planned, tween(800), label = "planned")
    val complete = doneToday > 0 && planned == 0 && LocalStudyOptions.current.celebrate
    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(cs.surfaceContainerHigh).border(1.dp, cs.outlineVariant, RoundedCornerShape(28.dp))) {
        Row(Modifier.fillMaxWidth().padding(20.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Column(Modifier.weight(1f).padding(end = 16.dp), Arrangement.spacedBy(3.dp)) {
                Text(s.todayTitle, style = MaterialTheme.typography.labelLarge, color = cs.onSurfaceVariant)
                FitText(if (planned > 0 || counts.total == 0) "$shown" else s.nothingDue, MaterialTheme.typography.displaySmall, Modifier.fillMaxWidth(), cs.onSurface, 2)
                Text(
                    when { counts.total == 0 -> s.noCards; planned > 0 -> "${counts.due} ${s.due} · ${counts.fresh} ${s.freshWord}"; else -> s.nothingDueHint },
                    style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant
                )
                if (counts.total > 0) {
                    Spacer(Modifier.height(10.dp))
                    GradientButton(if (planned > 0) s.startStudy else s.practiceMore, if (planned > 0) onStart else onExtra)
                    TextButton(onPreview, Modifier.fillMaxWidth().height(48.dp)) { Text(s.previewCards) }
                }
            }
            Box(Modifier.clip(RoundedCornerShape(22.dp)).background(Brush.linearGradient(LocalAccent.current.gradientColors())).padding(6.dp)) {
                StudyRing(progress, "$doneToday", s.doneWord)
            }
        }
        if (complete) Confetti(Modifier.matchParentSize())
    }
}

/** Экран «Учёба» вне сессии: дашборд или, внутри проекта/колоды, плитка узла. Библиотеку читает только он, а не сессия. */
@Composable
fun StudyHome(ui: StudyUi, sessionSize: Int, study: StudyViewModel, library: LibraryViewModel, onSession: (StudySession) -> Unit) {
    val lib by library.state.collectAsStateWithLifecycle()
    when {
        lib.failed -> Column(Modifier.padding(16.dp)) { Text(LocalStrings.current.libraryFailed, color = MaterialTheme.colorScheme.error) }
        !lib.loaded -> Unit
        ui.scoped -> StudyScopeView(ui, lib, sessionSize, study, onSession)
        else -> StudyDashboard(lib, sessionSize, study, onSession)
    }
}

/**
 * Карточка проекта на экране «Учёба»: название, счётчики к повтору и новых,
 * и кнопка старта сессии по этому проекту.
 */
@Composable
fun StudyProjectCard(name: String, counts: CardCounts, onStart: () -> Unit) {
    val s = LocalStrings.current
    val colors = MaterialTheme.colorScheme
    val hasWork = counts.due > 0 || counts.fresh > 0
    Surface(
        Modifier.fillMaxWidth(),
        RoundedCornerShape(20.dp),
        color = colors.surfaceContainerLow,
        border = BorderStroke(1.dp, colors.outlineVariant)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            Arrangement.SpaceBetween,
            Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f).padding(end = 12.dp), Arrangement.spacedBy(3.dp)) {
                FitText(name.capFirst(), MaterialTheme.typography.titleLarge, Modifier.fillMaxWidth(), colors.onSurface, 2)
                if (counts.total == 0) {
                    Text(s.noCards, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                } else if (hasWork) {
                    Text(
                        "${counts.due} ${s.due} · ${counts.fresh} ${s.freshWord}",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                } else {
                    Text(s.nothingDue, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                }
            }
            if (hasWork) StudyPlayButton(onClick = onStart)
        }
    }
}

/** Кольцо прогресса дня: белая дуга по полупрозрачной дорожке, внутри число и подпись. Рисуется на градиентной плитке. */
@Composable
fun StudyRing(progress: Float, value: String, caption: String, modifier: Modifier = Modifier) {
    val sweep by animateFloatAsState(progress.coerceIn(0f, 1f), tween(700), label = "ring")
    Box(modifier.size(98.dp), Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val inset = 6.dp.toPx()
            val box = Size(size.width - 2 * inset, size.height - 2 * inset)
            val stroke = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
            drawArc(Color.White.copy(alpha = 0.25f), -90f, 360f, false, Offset(inset, inset), box, style = stroke)
            if (sweep > 0f) drawArc(Color.White, -90f, 360f * sweep, false, Offset(inset, inset), box, style = stroke)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.titleLarge, color = Color.White)
            FitText(caption.capFirst(), MaterialTheme.typography.labelMedium, Modifier.widthIn(max = 64.dp), Color.White.copy(alpha = 0.8f))
        }
    }
}

@Composable
fun StudyTaskPopup(text: String, key: String) {
    var visible by remember(key) { mutableStateOf(false) }
    LaunchedEffect(key) {
        visible = true
        delay(1700)
        visible = false
    }
    Popup(alignment = Alignment.TopCenter, offset = IntOffset(0, -58), properties = PopupProperties(clippingEnabled = false)) {
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(initialOffsetY = { it / 2 }, animationSpec = tween(240)) + fadeIn(tween(240)),
            exit = slideOutVertically(targetOffsetY = { -it / 2 }, animationSpec = tween(240)) + fadeOut(tween(240))
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(.94f),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                tonalElevation = 6.dp
            ) {
                Text(
                    text.capFirst(),
                    Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.titleLarge.copy(textAlign = TextAlign.Center),
                    maxLines = 2
                )
            }
        }
    }
}

/** Внутри проекта или колоды: плитка «Сегодня» для этого узла и каталог вложенных колод. */
@Composable
fun StudyScopeView(ui: StudyUi, lib: LibraryState, sessionSize: Int, study: StudyViewModel, onSession: (StudySession) -> Unit) {
    val s = LocalStrings.current
    var grid by rememberSaveable { mutableStateOf(true) }
    val project = lib.items.firstOrNull { it.id == ui.projectId }
    val deck = project?.children?.firstOrNull { it.id == ui.deckId }
    val cards = StudyScope.cards(lib.items, ui.projectId, ui.deckId)
    fun counts(list: List<LibNode>) = CardCounts.of(list, lib.states, lib.today)
    fun start(extra: Boolean) { study.begin(SessionPicker.pick(cards, lib.states, lib.today, sessionSize, extra))?.let(onSession) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), Arrangement.spacedBy(14.dp)) {
        LibraryHeader(deck?.name ?: project?.name ?: s.tabStudy, true, study::back)
        if (cards.isEmpty()) Text(s.noCards, color = MaterialTheme.colorScheme.onSurfaceVariant)
        else {
            StudyHero(counts(cards), sessionSize, lib.stats.todayReviews, { start(false) }, { start(true) }, { study.beginRandom(cards)?.let(onSession) })
            if (ui.deckId == null) {
                ViewToggle(grid) { grid = it }
                StudyCatalog(project?.children.orEmpty(), 1, grid, { counts(it.children) }, { study.openDeck(it.id) })
            }
        }
    }
}

/** Кнопка «Учить» в цвете акцента: запускает случайный круг по всем карточкам проекта или колоды. */
@Composable
fun StudyPlayButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val label = LocalStrings.current.learn
    Box(
        modifier.size(48.dp).clip(RoundedCornerShape(13.dp)).background(Brush.linearGradient(LocalAccent.current.gradientColors()))
            .clickable(role = Role.Button, onClick = onClick).semantics { contentDescription = label },
        contentAlignment = Alignment.Center
    ) { AppIcon(R.drawable.ic_play, Color.White, 22.dp) }
}

/** Итог вчерашнего дня: сколько повторов и точность; без занятий — спокойное сообщение. */
@Composable
fun YesterdayCard(stats: StudyStats, modifier: Modifier = Modifier) {
    val s = LocalStrings.current
    DashCard(s.yesterdayTitle, modifier) {
        if (stats.yesterdayReviews == 0) Text(s.yesterdayEmpty, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        else Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("${stats.yesterdayReviews}", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.primary)
            Text(s.reviewsCount(stats.yesterdayReviews).substringAfter(' '), style = MaterialTheme.typography.bodyMedium)
            Text("${stats.yesterdayAccuracy}% ${s.statsAccuracy.lowercase()}", style = MaterialTheme.typography.labelLarge, color = Grade.KNOW.tint())
        }
    }
}

/** Плитка дашборда: подпись и содержимое, одинаковые контур и скругление во всём приложении. */
@Composable
fun DashCard(title: String, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(modifier.fillMaxHeight(), CardStyle.shape, colors.surfaceVariant, border = BorderStroke(1.dp, colors.outlineVariant)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = colors.onSurfaceVariant)
            content()
        }
    }
}

/** Короткая мотивирующая фраза с акцентной полосой слева и живым эмодзи. */
@Composable
fun MotivationBanner(text: String, emoji: String) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(24.dp)
    Row(
        Modifier.fillMaxWidth().height(IntrinsicSize.Min).clip(shape).background(colors.primaryContainer).border(1.dp, colors.primary.copy(alpha = 0.35f), shape).padding(16.dp),
        Arrangement.spacedBy(12.dp), Alignment.CenterVertically
    ) {
        Box(Modifier.width(4.dp).fillMaxHeight().clip(RoundedCornerShape(2.dp)).background(Brush.verticalGradient(LocalAccent.current.gradientColors())))
        PulseEmoji(emoji)
        Text(text, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
    }
}

/** Значок к мотивационной фразе дашборда. */
fun motivationEmoji(kind: MotivationKind): String = when (kind) {
    MotivationKind.EMPTY -> "📚"
    MotivationKind.START -> "🚀"
    MotivationKind.DONE_TODAY -> "🎉"
    MotivationKind.STREAK -> "🔥"
    MotivationKind.CATCH_UP -> "⏰"
    MotivationKind.MATURE -> "🌟"
    MotivationKind.KEEP -> "💪"
}
