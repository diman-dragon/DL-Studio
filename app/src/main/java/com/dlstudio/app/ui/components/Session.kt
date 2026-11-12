package com.dlstudio.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dlstudio.app.R
import com.dlstudio.app.study.CardPlan
import com.dlstudio.app.study.Grade
import com.dlstudio.app.study.PraiseKind
import com.dlstudio.app.study.SessionPraise
import com.dlstudio.app.study.Speak
import com.dlstudio.app.study.StudySession
import com.dlstudio.app.ui.theme.LocalAccent
import com.dlstudio.app.ui.theme.LocalStrings
import com.dlstudio.app.ui.theme.LocalStudyOptions
import com.dlstudio.app.ui.theme.gradientColors
import kotlin.math.sin
import kotlin.random.Random
import kotlinx.coroutines.delay

/** Верхняя строка сессии на компонентах: крестик «закрыть», полоса прогресса и счётчик. */
@Composable
fun SessionHeader(finished: Int, total: Int, onClose: () -> Unit, taskText: String? = null, taskKey: String = "") {
    Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(14.dp), Alignment.CenterVertically) {
        CloseButton(onClose)
        androidx.compose.foundation.layout.Box(Modifier.weight(1f).height(10.dp), contentAlignment = Alignment.Center) {
            SessionProgress(finished, total)
            if (taskText != null) StudyTaskPopup(taskText, taskKey)
        }
        Text(LocalStrings.current.sessionProgress(finished, total), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** Компонентный индикатор прогресса прохождения сессии. */
@Composable
fun SessionProgress(finished: Int, total: Int, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val fraction = if (total == 0) 0f else (finished.toFloat() / total).coerceIn(0f, 1f)
    Box(modifier.height(10.dp).clip(RoundedCornerShape(5.dp)).background(colors.surfaceVariant)) {
        Box(Modifier.fillMaxHeight().fillMaxWidth(fraction).clip(RoundedCornerShape(5.dp)).background(colors.primary))
    }
}

/** Итог сессии с похвалой по результату (при «отлично» — конфетти); в режиме «Учить» сообщает о новом круге в другом порядке. */
@Composable
fun SessionDone(session: StudySession, onBack: () -> Unit) {
    val s = LocalStrings.current
    val level = SessionPraise.level(session.know, session.total)
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(24.dp), Arrangement.Center, Alignment.CenterHorizontally) {
            Box(Modifier.size(96.dp).background(Brush.linearGradient(LocalAccent.current.gradientColors()), CircleShape), contentAlignment = Alignment.Center) {
                AppIcon(R.drawable.ic_check, Color.White, 52.dp)
            }
            Text(s.sessionDone, Modifier.padding(top = 20.dp), style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
            Text(s.sessionPraise(level), Modifier.padding(top = 6.dp), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center)
            Text(
                s.sessionSummary(session.know, session.hard, session.again), Modifier.padding(top = 6.dp, bottom = 12.dp),
                style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center
            )
            if (session.random) Text(s.newRound, Modifier.padding(bottom = 20.dp), color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center)
            GradientButton(s.backToList, onBack)
        }
        if (level == 2 && LocalStudyOptions.current.celebrate) Confetti(Modifier.fillMaxSize())
    }
}

/** Сессия: карточки листаются свайпом, ниже карточки варианты ответа. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StudySessionView(
    session: StudySession,
    onFlip: (String, Grade?) -> Unit,
    onHint: (String) -> Unit,
    onPage: (Int) -> Unit,
    onNextRound: () -> Unit,
    onClose: () -> Unit,
    onSpeak: Speak
) {
    key(session.id) {
        val pager = rememberPagerState(initialPage = session.index) { session.total + 1 }
        val card = session.current
        val plan = card?.let { CardPlan.of(it, session.index, session.queue, !session.random && it.back.isNotBlank(), LocalStudyOptions.current) }
        val task = plan?.mode?.takeIf { !it.flip }?.let { LocalStrings.current.taskText(it) }
        LaunchedEffect(pager) { snapshotFlow { pager.currentPage }.collect { onPage(it) } }
        LaunchedEffect(pager.currentPage) { if (session.random && pager.currentPage == session.total) { delay(1800); onNextRound() } }
        Column(Modifier.fillMaxSize().padding(16.dp), Arrangement.spacedBy(16.dp)) {
            SessionHeader(session.finished, session.total, onClose, task, "${card?.id}:$task")
            HorizontalPager(pager, Modifier.weight(1f), pageSpacing = 16.dp, key = { it }) { page ->
                val pageCard = session.queue.getOrNull(page)
                if (pageCard == null) SessionDone(session, onClose)
                else StudyCardPage(pageCard, page, session, pager.currentPage == page, onFlip, onHint, onSpeak)
            }
        }
    }
}

/** Конфетти: один раз сыплется сверху вниз и гаснет (около 2,5 секунд). Частицы считаются из номера, поэтому ничего не хранится. */
@Composable
fun Confetti(modifier: Modifier = Modifier, count: Int = 42) {
    val time = remember { Animatable(0f) }
    LaunchedEffect(Unit) { time.animateTo(1f, tween(2600, easing = LinearEasing)) }
    val palette = remember { listOf(Color(0xFFF59E0B), Color(0xFF2563EB), Color(0xFF10B981), Color(0xFFEC4899), Color(0xFF7C3AED)) }
    Canvas(modifier) {
        val t = time.value
        if (t < 1f) repeat(count) { i ->
            val r = Random(i * 7919)
            val delay = r.nextFloat() * 0.35f
            val p = ((t - delay) / (1f - delay)).coerceIn(0f, 1f)
            val x = (r.nextFloat() + sin(p * 9f + i) * 0.025f) * size.width
            val y = -24f + p * (size.height * (0.7f + r.nextFloat() * 0.5f))
            val side = (6 + r.nextInt(7)) * density
            val color = palette[i % palette.size].copy(alpha = (1.4f - p).coerceIn(0f, 1f))
            if (p > 0f) rotate(p * 540f * (if (i % 2 == 0) 1 else -1), Offset(x, y)) { drawRect(color, Offset(x, y), Size(side, side * 0.55f)) }
        }
    }
}

/** Значок к похвале на экране статистики. */
fun praiseEmoji(kind: PraiseKind): String = when (kind) {
    PraiseKind.RECORD_DAY -> "🏆"
    PraiseKind.UP_TODAY -> "📈"
    PraiseKind.BEST_STREAK -> "🔥"
    PraiseKind.WEEK_UP -> "🚀"
    PraiseKind.ACCURACY -> "🎯"
    PraiseKind.KEEP -> "💪"
}

/** Эмодзи, которое мягко «дышит» и покачивается; при выключенных анимациях в настройках — просто неподвижный значок. */
@Composable
fun PulseEmoji(emoji: String, modifier: Modifier = Modifier) {
    if (!LocalStudyOptions.current.celebrate) return EmojiText(emoji, modifier)
    val transition = rememberInfiniteTransition(label = "emoji")
    val scale by transition.animateFloat(0.9f, 1.18f, infiniteRepeatable(tween(750, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "scale")
    val tilt by transition.animateFloat(-7f, 7f, infiniteRepeatable(tween(950, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "tilt")
    EmojiText(emoji, modifier.graphicsLayer { scaleX = scale; scaleY = scale; rotationZ = tilt })
}
