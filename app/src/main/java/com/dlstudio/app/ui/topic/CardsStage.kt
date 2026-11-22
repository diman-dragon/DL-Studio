package com.dlstudio.app.ui.topic

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dlstudio.app.content.Card
import com.dlstudio.app.speech.Speak
import com.dlstudio.app.ui.components.GradientButton
import com.dlstudio.app.ui.components.ProgressBar
import com.dlstudio.app.ui.components.SmallSpeaker
import com.dlstudio.app.ui.theme.LocalStrings

/**
 * Карточки по порядку: тап переворачивает; увидел ответ — карточка засчитана и даёт XP (один раз). Никаких «знаю / не знаю»:
 * учитывается только то, что пользователь сделал. Колода идёт по кругу, пока не просмотрены все карточки; расписания повторений нет.
 */
@Composable
fun CardsStage(cards: List<Card>, seen: Set<Int>, speak: Speak?, onSeen: (Int) -> Unit, modifier: Modifier = Modifier) {
    val s = LocalStrings.current
    var index by rememberSaveable { mutableStateOf(0) }
    var flipped by rememberSaveable(index) { mutableStateOf(false) }
    val at = index.coerceIn(0, cards.lastIndex)
    Column(modifier.fillMaxSize().padding(16.dp), Arrangement.spacedBy(14.dp)) {
        Text(s.cardCounter(at + 1, cards.size, seen.size), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        ProgressBar(seen.size / cards.size.toFloat())
        FlipCard(cards[at], flipped, speak, Modifier.weight(1f)) { flipped = !flipped; if (flipped) onSeen(at) }
        if (flipped) {
            GradientButton(s.next, { index = (at + 1) % cards.size })
        } else {
            Text(s.tapToFlip, Modifier.fillMaxWidth().padding(vertical = 18.dp), MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

/** Карточка с объёмным переворотом: до половины поворота видно лицо, дальше — оборот (повёрнутый обратно, чтобы читался). */
@Composable
private fun FlipCard(card: Card, flipped: Boolean, speak: Speak?, modifier: Modifier, onFlip: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(28.dp)
    val angle by animateFloatAsState(if (flipped) 180f else 0f, tween(350), label = "flip")
    Box(
        modifier.fillMaxWidth().graphicsLayer { rotationY = angle; cameraDistance = 14f * density }
            .shadow(20.dp, shape, ambientColor = colors.primary, spotColor = colors.primary).clip(shape)
            .background(Brush.linearGradient(listOf(colors.primaryContainer.copy(alpha = 0.55f), colors.surfaceVariant)))
            .border(1.dp, colors.primary.copy(alpha = 0.4f), shape)
            .clickable(role = Role.Button, onClick = onFlip).padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        if (angle <= 90f) Front(card, speak) else Box(Modifier.graphicsLayer { rotationY = 180f }) { Back(card, speak) }
    }
}

@Composable
private fun Front(card: Card, speak: Speak?) {
    Column(Modifier.fillMaxWidth(), Arrangement.spacedBy(16.dp), Alignment.CenterHorizontally) {
        Text(card.front, style = MaterialTheme.typography.headlineLarge, textAlign = TextAlign.Center)
        if (speak != null) SmallSpeaker(card.front, speak)
    }
}

@Composable
private fun Back(card: Card, speak: Speak?) {
    val colors = MaterialTheme.colorScheme
    Column(Modifier.fillMaxWidth(), Arrangement.spacedBy(12.dp), Alignment.CenterHorizontally) {
        Text(card.back, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
        if (card.ipa.isNotBlank()) Text(card.ipa, style = MaterialTheme.typography.titleMedium, color = colors.primary)
        if (card.example.isNotBlank()) {
            Text(card.example, style = MaterialTheme.typography.bodyLarge, fontStyle = FontStyle.Italic, color = colors.onSurfaceVariant, textAlign = TextAlign.Center)
            if (speak != null) SmallSpeaker(card.example, speak)
        }
    }
}
