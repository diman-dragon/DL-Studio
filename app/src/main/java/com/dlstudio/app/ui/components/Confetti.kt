package com.dlstudio.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.sin
import kotlin.random.Random

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
