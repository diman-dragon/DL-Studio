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
import com.dlstudio.app.study.CardPlan
import com.dlstudio.app.study.Speak
import kotlin.math.abs

/** Карточка с переворотом: слово по центру максимально крупно; переворот заблокирован, пока нет ответа (`canFlip`). */
@Composable
fun StudyFlashCard(
    card: LibNode, plan: CardPlan, flipped: Boolean, hint: Boolean, hintReady: Boolean, listenRevealed: Boolean, canFlip: Boolean,
    onFlip: () -> Unit, onHint: () -> Unit, onSpeak: Speak, pronounce: PronounceState, modifier: Modifier = Modifier
) {
    val turn by animateFloatAsState(if (flipped) 1f else 0f, tween(260, easing = FastOutSlowInEasing), label = "flip")
    val shape = RoundedCornerShape(26.dp)
    val cs = MaterialTheme.colorScheme
    Box(
        modifier = modifier.fillMaxWidth()
            .clickable(remember { MutableInteractionSource() }, null, enabled = canFlip, onClick = onFlip)
            .graphicsLayer { scaleX = abs(1f - 2f * turn).coerceAtLeast(0.02f) }
            .clip(shape).background(cs.surfaceContainerHigh).border(1.dp, cs.outlineVariant, shape).padding(16.dp)
    ) {
        if (turn < 0.5f) FlashFront(card, plan.mode, plan.target, hint, hintReady, listenRevealed, onHint, onSpeak, pronounce)
        else FlashBack(card, plan.mode, onSpeak, pronounce)
    }
}
