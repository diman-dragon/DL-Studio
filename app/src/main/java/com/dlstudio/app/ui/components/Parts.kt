package com.dlstudio.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dlstudio.app.ui.theme.LocalAccent
import com.dlstudio.app.ui.theme.gradientColors

/** Заголовок экрана. */
@Composable
fun ScreenHeader(text: String, modifier: Modifier = Modifier) {
    Text(text, modifier.fillMaxWidth().padding(bottom = 16.dp), style = MaterialTheme.typography.headlineMedium, maxLines = 1)
}

/** Небольшая «таблетка» с коротким текстом (уровень, серия); `accent` — заливка акцентом. */
@Composable
fun Pill(text: String, modifier: Modifier = Modifier, accent: Boolean = false) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier, RoundedCornerShape(16.dp), if (accent) colors.primaryContainer else colors.surfaceVariant,
        border = BorderStroke(1.dp, if (accent) colors.primary.copy(alpha = 0.5f) else colors.outlineVariant)
    ) {
        Text(
            text, Modifier.padding(horizontal = 12.dp, vertical = 6.dp), if (accent) colors.primary else colors.onSurfaceVariant,
            style = MaterialTheme.typography.labelLarge, maxLines = 1
        )
    }
}

/** Полоса прогресса в цветах акцента; `fraction` от 0 до 1. */
@Composable
fun ProgressBar(fraction: Float, modifier: Modifier = Modifier, height: Dp = 8.dp) {
    val track = MaterialTheme.colorScheme.outline
    val shape = RoundedCornerShape(height)
    Box(modifier.fillMaxWidth().height(height).clip(shape).background(track)) {
        Box(Modifier.fillMaxHeight().fillMaxWidth(fraction.coerceIn(0f, 1f)).clip(shape).background(Brush.linearGradient(LocalAccent.current.gradientColors())))
    }
}

/** Кольцо прогресса (`fraction` от 0 до 1) с содержимым по центру. */
@Composable
fun Ring(fraction: Float, diameter: Dp, thickness: Dp, modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val accent = Color(LocalAccent.current.main)
    val track = MaterialTheme.colorScheme.outline
    Box(modifier.size(diameter), Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val w = thickness.toPx()
            val corner = Offset(w / 2, w / 2)
            val box = Size(size.width - w, size.height - w)
            drawArc(track, 0f, 360f, false, corner, box, style = Stroke(w))
            drawArc(accent, -90f, 360f * fraction.coerceIn(0f, 1f), false, corner, box, style = Stroke(w, cap = StrokeCap.Round))
        }
        content()
    }
}
