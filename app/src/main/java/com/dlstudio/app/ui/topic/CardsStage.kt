package com.dlstudio.app.ui.topic

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dlstudio.app.content.Card
import com.dlstudio.app.speech.Check
import com.dlstudio.app.speech.Speak
import com.dlstudio.app.ui.components.GradientButton
import com.dlstudio.app.ui.components.ProgressBar
import com.dlstudio.app.ui.components.PronounceRow
import com.dlstudio.app.ui.theme.LocalAccent
import com.dlstudio.app.ui.theme.LocalStrings
import com.dlstudio.app.ui.theme.gradientColors
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sin

private const val FLIP_MS = 650
private const val LONG_TEXT = 18
private const val VERY_LONG_TEXT = 32

/**
 * Карточки: листаются свайпом (соседние карточки чуть повёрнуты и уменьшены), тап плавно переворачивает. Увидел ответ — карточка засчитана
 * и даёт XP (один раз). Никаких «знаю / не знаю»: учитывается только то, что пользователь сделал.
 * Лицо: часть речи, слово, транскрипция; оборот: перевод, формы, пример с переводом, объяснение — у каждого поля своё место, пустые не рисуются.
 * Озвучка и микрофон (проверка произношения) — только у слова и примера, то есть только на изучаемом языке.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CardsStage(cards: List<Card>, seen: Set<Int>, speak: Speak?, check: Check?, onSeen: (Int) -> Unit, modifier: Modifier = Modifier) {
    val s = LocalStrings.current
    val pager = rememberPagerState { cards.size }
    val scope = rememberCoroutineScope()
    val flipped = remember { mutableStateMapOf<Int, Boolean>() }
    val at = pager.currentPage
    LaunchedEffect(at) { check?.cancel?.invoke() }
    Column(modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp), Arrangement.spacedBy(12.dp)) {
        CounterChip(s.cardCounter(at + 1, cards.size, seen.size))
        ProgressBar(seen.size / cards.size.toFloat())
        HorizontalPager(pager, Modifier.weight(1f), pageSpacing = 0.dp) { page ->
            FlipCard(cards[page], flipped[page] == true, speak, check, Modifier.fillMaxSize().padding(horizontal = 10.dp, vertical = 12.dp).pageEffect(pager, page)) {
                val now = flipped[page] != true
                flipped[page] = now
                if (now) onSeen(page)
            }
        }
        if (flipped[at] == true) {
            GradientButton(s.next, { scope.launch { pager.animateScrollToPage((at + 1) % cards.size) } })
        } else {
            Text(s.tapToFlip, Modifier.fillMaxWidth().padding(vertical = 18.dp), MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun CounterChip(text: String) {
    val colors = MaterialTheme.colorScheme
    Text(
        text, Modifier.clip(CircleShape).background(colors.primaryContainer).padding(horizontal = 12.dp, vertical = 4.dp),
        style = MaterialTheme.typography.labelLarge, color = colors.onPrimaryContainer
    )
}

/** Эффект листания: чем дальше карточка от центра, тем она меньше, прозрачнее и сильнее повёрнута вокруг вертикальной оси. */
@OptIn(ExperimentalFoundationApi::class)
private fun Modifier.pageEffect(pager: PagerState, page: Int): Modifier = graphicsLayer {
    val offset = (pager.currentPage - page) + pager.currentPageOffsetFraction
    val far = abs(offset).coerceIn(0f, 1f)
    val k = 1f - 0.12f * far
    scaleX = k; scaleY = k
    alpha = 1f - 0.5f * far
    rotationY = offset * -18f
    cameraDistance = 14f * density
}

/**
 * Карточка с плавным объёмным переворотом: поворот вокруг вертикальной оси с мягким замедлением и лёгким «вдохом» (карточка чуть уменьшается
 * на середине поворота). До половины видно лицо, дальше — оборот, повёрнутый обратно, чтобы читался.
 */
@Composable
private fun FlipCard(card: Card, flipped: Boolean, speak: Speak?, check: Check?, modifier: Modifier, onFlip: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(32.dp)
    val angle by animateFloatAsState(if (flipped) 180f else 0f, tween(FLIP_MS, easing = FastOutSlowInEasing), label = "flip")
    val back = angle > 90f
    val glow = if (back) LocalAccent.current.gradientColors().first() else colors.primary
    Box(
        modifier.graphicsLayer {
            rotationY = angle
            cameraDistance = 16f * density
            val dip = 1f - 0.07f * sin(Math.toRadians(angle.toDouble())).toFloat()
            scaleX = dip; scaleY = dip
        }
            .shadow(22.dp, shape, ambientColor = glow, spotColor = glow).clip(shape)
            .background(if (back) Brush.linearGradient(LocalAccent.current.gradientColors()) else Brush.linearGradient(listOf(colors.primaryContainer, colors.surfaceVariant)))
            .background(if (back) Color.Black.copy(alpha = 0.2f) else Color.Transparent)
            .decor(if (back) Color.White else colors.primary)
            .border(1.dp, glow.copy(alpha = 0.45f), shape)
            .clickable(role = Role.Button, onClick = onFlip).padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        if (!back) Front(card, speak, check) else Box(Modifier.graphicsLayer { rotationY = 180f }) { Back(card, speak, check) }
    }
}

/** Мягкие круги-украшения в углах карточки. */
private fun Modifier.decor(tint: Color): Modifier = drawBehind {
    drawCircle(tint.copy(alpha = 0.10f), size.minDimension * 0.55f, Offset(size.width * 0.95f, size.height * 0.04f))
    drawCircle(tint.copy(alpha = 0.07f), size.minDimension * 0.4f, Offset(size.width * 0.02f, size.height * 0.98f))
}

/** Лицо: часть речи, слово на изучаемом языке, транскрипция и озвучка с проверкой произношения. Пустые поля не показываются. */
@Composable
private fun Front(card: Card, speak: Speak?, check: Check?) {
    val colors = MaterialTheme.colorScheme
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), Arrangement.spacedBy(14.dp), Alignment.CenterHorizontally) {
        if (card.pos.isNotBlank()) {
            Text(
                card.pos, Modifier.clip(CircleShape).background(colors.primary.copy(alpha = 0.18f)).padding(horizontal = 12.dp, vertical = 3.dp),
                style = MaterialTheme.typography.labelLarge, color = colors.primary
            )
        }
        Text(card.front, style = sized(card.front), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        if (card.ipa.isNotBlank()) Text(card.ipa, style = MaterialTheme.typography.titleMedium, color = colors.primary, textAlign = TextAlign.Center)
        if (speak != null) PronounceRow(card.say.ifBlank { card.front }, speak, check.takeIf { card.mic })
    }
}

/**
 * Оборот: перевод, формы слова, пример на изучаемом языке (с озвучкой и проверкой) и его перевод, объяснение. Озвучка и микрофон только у примера:
 * перевод и объяснение написаны не на изучаемом языке. Пустые поля не показываются.
 */
@Composable
private fun Back(card: Card, speak: Speak?, check: Check?) {
    val white = Color.White
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), Arrangement.spacedBy(14.dp), Alignment.CenterHorizontally) {
        Text(card.back, style = sized(card.back), color = white, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        if (card.forms.isNotBlank()) {
            Text(
                card.forms, Modifier.clip(CircleShape).background(white.copy(alpha = 0.22f)).padding(horizontal = 16.dp, vertical = 6.dp),
                style = MaterialTheme.typography.titleMedium, color = white, textAlign = TextAlign.Center
            )
        }
        if (card.example.isNotBlank() || card.exampleTr.isNotBlank()) {
            Panel(0.16f) {
                if (card.example.isNotBlank()) {
                    Text(card.example, style = MaterialTheme.typography.bodyLarge, fontStyle = FontStyle.Italic, color = white, textAlign = TextAlign.Center)
                    if (speak != null) PronounceRow(card.example, speak, check)
                }
                if (card.exampleTr.isNotBlank()) Text(card.exampleTr, style = MaterialTheme.typography.bodyMedium, color = white.copy(alpha = 0.85f), textAlign = TextAlign.Center)
            }
        }
        if (card.note.isNotBlank()) Panel(0.1f) { Text("💡 " + card.note, style = MaterialTheme.typography.bodyMedium, color = white, textAlign = TextAlign.Center) }
    }
}

/** Светлая плашка на обороте карточки для группы связанных полей. */
@Composable
private fun Panel(alpha: Float, content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Color.White.copy(alpha = alpha)).padding(14.dp),
        Arrangement.spacedBy(8.dp), Alignment.CenterHorizontally, content = content
    )
}

/** Размер шрифта по длине текста: короткие слова крупно, длинные фразы мельче, чтобы не обрезались. */
@Composable
private fun sized(text: String) = when {
    text.length > VERY_LONG_TEXT -> MaterialTheme.typography.titleLarge
    text.length > LONG_TEXT -> MaterialTheme.typography.headlineSmall
    else -> MaterialTheme.typography.headlineLarge
}
