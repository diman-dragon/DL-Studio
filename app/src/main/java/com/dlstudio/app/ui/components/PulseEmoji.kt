package com.dlstudio.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.dlstudio.app.ui.theme.LocalStudyOptions

/** Эмодзи, которое мягко «дышит» и покачивается; при выключенных анимациях в настройках — просто неподвижный значок. */
@Composable
fun PulseEmoji(emoji: String, modifier: Modifier = Modifier, fontSize: TextUnit = 28.sp) {
    if (!LocalStudyOptions.current.celebrate) return Text(emoji, modifier, fontSize = fontSize)
    val transition = rememberInfiniteTransition(label = "emoji")
    val scale by transition.animateFloat(0.9f, 1.18f, infiniteRepeatable(tween(750, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "scale")
    val tilt by transition.animateFloat(-7f, 7f, infiniteRepeatable(tween(950, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "tilt")
    Text(emoji, modifier.graphicsLayer { scaleX = scale; scaleY = scale; rotationZ = tilt }, fontSize = fontSize)
}
