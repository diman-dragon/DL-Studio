package com.dlstudio.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dlstudio.app.ui.theme.LocalAccent
import com.dlstudio.app.ui.theme.LocalStrings
import com.dlstudio.app.ui.theme.gradientColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Верх стартового экрана: серия (пламя дышит), набранный XP (число набегает) и уровень. Нет серии — пламя серое. */
@Composable
fun TopChips(streak: Int, xp: Int, level: Int) {
    val s = LocalStrings.current
    val flame by rememberInfiniteTransition(label = "flame").animateFloat(0.92f, 1.14f, infiniteRepeatable(tween(650), RepeatMode.Reverse), label = "flame")
    Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp), Alignment.CenterVertically) {
        StatChip("🔥", "$streak", streak > 0, if (streak > 0) flame else 1f)
        StatChip("⚡", "${rememberCountUp(xp)} XP", xp > 0)
        StatChip("🏅", s.levelTitle(level), true)
    }
}

@Composable
private fun StatChip(emoji: String, text: String, lit: Boolean, scale: Float = 1f) {
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(18.dp), color = if (lit) colors.primaryContainer else colors.surfaceVariant,
        border = BorderStroke(1.dp, if (lit) colors.primary.copy(alpha = 0.5f) else colors.outlineVariant)
    ) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 7.dp), Arrangement.spacedBy(6.dp), Alignment.CenterVertically) {
            Text(emoji, Modifier.graphicsLayer { scaleX = scale; scaleY = scale; alpha = if (lit) 1f else 0.4f }, style = MaterialTheme.typography.titleMedium)
            Text(text, style = MaterialTheme.typography.titleMedium, color = if (lit) colors.primary else colors.onSurfaceVariant, maxLines = 1)
        }
    }
}

/** Печатает `text` по буквам; место под весь текст занято сразу, поэтому ничего не прыгает. */
@Composable
private fun Typewriter(text: String, color: Color) {
    var shown by remember(text) { mutableStateOf(0) }
    LaunchedEffect(text) { while (shown < text.length) { delay(20); shown += 1 } }
    val cut = if (shown in 1 until text.length && Character.isHighSurrogate(text[shown - 1])) shown + 1 else shown
    val style = MaterialTheme.typography.bodyLarge
    Box {
        Text(text, style = style, color = Color.Transparent)
        Text(text.take(cut), style = style, color = color)
    }
}

/**
 * Талисман с репликой: лисёнок покачивается (а когда цель дня выполнена — приплясывает), по тапу подпрыгивает и говорит новую подбадривающую фразу.
 * Реплика печатается по буквам.
 */
@Composable
fun MascotBubble(text: String, happy: Boolean, onTap: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val idle by rememberInfiniteTransition(label = "mascot").animateFloat(0f, 1f, infiniteRepeatable(tween(if (happy) 450 else 1700, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "idle")
    val jump = remember { Animatable(0f) }
    val lift = if (happy) 12.dp else 5.dp
    Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(12.dp), Alignment.Bottom) {
        Box(
            Modifier.size(72.dp).graphicsLayer { translationY = -(idle * lift.toPx() + jump.value * 30.dp.toPx()); rotationZ = (idle - 0.5f) * (if (happy) 12f else 4f) }
                .clip(CircleShape).background(Brush.linearGradient(LocalAccent.current.gradientColors()))
                .clickable(role = Role.Button) {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    scope.launch { jump.snapTo(0f); jump.animateTo(1f, tween(140)); jump.animateTo(0f, spring(Spring.DampingRatioMediumBouncy)) }
                    onTap()
                },
            Alignment.Center
        ) { Text("🦊", style = MaterialTheme.typography.displaySmall) }
        Surface(Modifier.weight(1f), RoundedCornerShape(22.dp, 22.dp, 22.dp, 6.dp), colors.surfaceVariant, border = BorderStroke(1.dp, colors.outlineVariant)) {
            Box(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) { Typewriter(text, colors.onSurface) }
        }
    }
}

/**
 * Неделя кольцами: у каждого дня кольцо «сколько от цели». Цель выполнена — золотой круг с галочкой, сегодняшний день подписан акцентом.
 * Кольца выскакивают по очереди.
 */
@Composable
fun WeekCard(xps: List<Int>, goal: Int, today: Int) {
    val s = LocalStrings.current
    val colors = MaterialTheme.colorScheme
    SettingCard(gap = 12.dp) {
        CardTitle(s.weekTitle, s.weekTotal(xps.sum()))
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
            xps.forEachIndexed { day, xp ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    DayRing(xp, goal, day, day == today)
                    Text(
                        s.weekdays[day], style = MaterialTheme.typography.labelMedium, maxLines = 1,
                        color = if (day == today) colors.primary else colors.onSurfaceVariant, fontWeight = if (day == today) FontWeight.Bold else null
                    )
                }
            }
        }
    }
}

@Composable
private fun DayRing(xp: Int, goal: Int, index: Int, today: Boolean) {
    val accent = Color(LocalAccent.current.main)
    val track = MaterialTheme.colorScheme.outline
    val met = xp >= goal
    val pop = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(350L + index * 80L)
        pop.animateTo(1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow))
    }
    val fraction = rememberGrow((xp / goal.toFloat()).coerceIn(0f, 1f))
    Box(Modifier.size(38.dp).graphicsLayer { scaleX = pop.value; scaleY = pop.value }, Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val w = 5.dp.toPx()
            val box = Size(size.width - w, size.height - w)
            if (met) drawCircle(Brush.verticalGradient(GOLD))
            else {
                drawArc(if (today) accent.copy(alpha = 0.35f) else track, 0f, 360f, false, Offset(w / 2, w / 2), box, style = Stroke(w))
                drawArc(accent, -90f, 360f * fraction, false, Offset(w / 2, w / 2), box, style = Stroke(w, cap = StrokeCap.Round))
            }
        }
        if (met) Text("✓", color = Color(0xFF3A2400), style = MaterialTheme.typography.titleMedium)
    }
}
