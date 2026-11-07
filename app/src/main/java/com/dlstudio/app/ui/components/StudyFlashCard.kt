package com.dlstudio.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.dlstudio.app.library.LibNode
import com.dlstudio.app.study.CardMode
import com.dlstudio.app.study.SpeechLang
import com.dlstudio.app.ui.theme.LocalSpeechTag
import kotlin.math.abs

/** Уменьшенная аккуратная карточка с центрированным словом и блокировкой переворота до ответа. */
@Composable
fun StudyFlashCard(
    card: LibNode,
    mode: CardMode,
    flipped: Boolean,
    hint: Boolean,
    hintReady: Boolean,
    listenRevealed: Boolean,
    canFlip: Boolean,
    onFlip: () -> Unit,
    onHint: () -> Unit,
    onSpeak: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val turn by animateFloatAsState(if (flipped) 1f else 0f, tween(260, easing = FastOutSlowInEasing), label = "flip")
    val shape = RoundedCornerShape(26.dp)
    val cs = MaterialTheme.colorScheme
    val pronounce = rememberPronounce(card.name, SpeechLang.forText(card.name, LocalSpeechTag.current))
    Box(
        modifier = modifier.fillMaxWidth()
            .clickable(remember { MutableInteractionSource() }, null, enabled = canFlip, onClick = onFlip)
            .graphicsLayer { scaleX = abs(1f - 2f * turn).coerceAtLeast(0.02f) }
            .clip(shape).background(cs.surfaceContainerHigh).border(1.dp, cs.outlineVariant, shape).padding(20.dp)
    ) {
        if (turn < 0.5f) {
            FlashFront(card, mode, hint, hintReady, listenRevealed, onHint, onSpeak, pronounce)
        } else {
            FlashBack(card, mode, onSpeak, pronounce)
        }
    }
}
