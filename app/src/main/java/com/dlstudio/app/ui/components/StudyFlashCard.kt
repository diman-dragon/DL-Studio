package com.dlstudio.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.dlstudio.app.library.LibNode
import com.dlstudio.app.ui.theme.LocalAccent
import com.dlstudio.app.ui.theme.gradientColors

/** Карточка с поворотом на 180°: слово → перевод. Переворачивается тапом по лицевой стороне; подсказка и озвучка — кнопками. */
@Composable
fun StudyFlashCard(card: LibNode, flipped: Boolean, onFlip: () -> Unit, onSpeak: (String) -> Unit, modifier: Modifier = Modifier) {
    val turn by animateFloatAsState(if (flipped) 180f else 0f, tween(360, easing = FastOutSlowInEasing), label = "flip")
    var hint by remember(card.id) { mutableStateOf(false) }
    val showBack = turn > 90f
    val shape = RoundedCornerShape(30.dp)
    val colors = MaterialTheme.colorScheme
    val rim = Brush.linearGradient(LocalAccent.current.gradientColors().map { it.copy(alpha = 0.7f) })
    Column(
        modifier.fillMaxWidth().graphicsLayer { rotationY = turn; cameraDistance = 24f * density }
            .clip(shape).background(colors.surfaceVariant).border(1.5.dp, rim, shape)
            .clickable(enabled = !flipped, onClick = onFlip)
            .graphicsLayer { if (showBack) rotationY = 180f }.padding(26.dp),
        Arrangement.SpaceBetween, Alignment.CenterHorizontally
    ) {
        if (!showBack) FlashFront(card, hint, { hint = !hint }, onSpeak) else FlashBack(card, onSpeak)
    }
}
