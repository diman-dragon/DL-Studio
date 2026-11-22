package com.dlstudio.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.dlstudio.app.ui.theme.LocalAccent
import kotlin.math.sin
import kotlin.random.Random

/** Одна конфетина: где стартует по ширине (0..1), когда (0..1 от длительности), скорость падения, качание, размер, цвет и поворот. */
private class Piece(val x: Float, val delay: Float, val speed: Float, val sway: Float, val side: Float, val color: Int, val spin: Float)

/** Салют на весь экран, когда пройдена тема. `id` — номер награды: новый номер запускает салют заново (и он каждый раз разный). Касаний не перехватывает. */
@Composable
fun Confetti(id: Int, modifier: Modifier = Modifier) {
    val accent = LocalAccent.current
    val palette = listOf(Color(accent.soft), Color(accent.main), Color(accent.deep), Color(0xFFFFC857), Color(0xFFFF6B8B))
    val pieces = remember(id) {
        val random = Random(id)
        List(56) {
            Piece(random.nextFloat(), random.nextFloat() * 0.35f, 0.7f + random.nextFloat() * 0.6f, (random.nextFloat() - 0.5f) * 0.25f, 6f + random.nextFloat() * 8f, random.nextInt(5), random.nextFloat() * 360f)
        }
    }
    val time = remember(id) { Animatable(0f) }
    LaunchedEffect(id) { time.animateTo(1f, tween(2600, easing = LinearEasing)) }
    Canvas(modifier.fillMaxSize()) {
        val unit = 1.dp.toPx()
        pieces.forEach { p ->
            val k = ((time.value - p.delay) / (1f - p.delay)).coerceIn(0f, 1f)
            if (k > 0f && k < 1f) {
                val center = Offset((p.x + p.sway * sin(k * 9f)) * size.width, -20f + k * p.speed * (size.height + 40f))
                val side = p.side * unit
                rotate(p.spin + k * 540f, center) {
                    drawRect(palette[p.color].copy(alpha = 1f - k * k), Offset(center.x - side / 2, center.y - side / 3), Size(side, side * 0.6f))
                }
            }
        }
    }
}
