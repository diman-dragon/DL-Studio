package com.dlstudio.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.dlstudio.app.library.LibNode
import com.dlstudio.app.ui.theme.LocalAccent
import com.dlstudio.app.ui.theme.gradientColors
import kotlin.math.abs

/**
 * Карточка: тап показывает перевод или возвращает к слову. «Переворот» — сжатие по ширине до нуля и обратно
 * со сменой стороны посередине. Область нажатия лежит снаружи анимированного слоя, поэтому тап срабатывает всегда,
 * в том числе во время анимации; подсказка и озвучка — значки на карточке.
 */
@Composable
fun StudyFlashCard(card: LibNode, flipped: Boolean, hint: Boolean, onFlip: () -> Unit, onHint: () -> Unit, onSpeak: (String) -> Unit, modifier: Modifier = Modifier) {
    val turn by animateFloatAsState(if (flipped) 1f else 0f, tween(280, easing = FastOutSlowInEasing), label = "flip")
    val shape = RoundedCornerShape(30.dp)
    val rim = Brush.linearGradient(LocalAccent.current.gradientColors().map { it.copy(alpha = 0.7f) })
    Column(
        modifier.fillMaxWidth().clickable(remember { MutableInteractionSource() }, null, onClick = onFlip)
            .graphicsLayer { scaleX = abs(1f - 2f * turn).coerceAtLeast(0.02f) }
            .clip(shape).background(MaterialTheme.colorScheme.surfaceVariant).border(1.5.dp, rim, shape).padding(26.dp),
        Arrangement.SpaceBetween, Alignment.CenterHorizontally
    ) {
        if (turn < 0.5f) FlashFront(card, hint, onHint, onSpeak) else FlashBack(card, onSpeak)
    }
}
