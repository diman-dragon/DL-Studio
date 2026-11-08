package com.dlstudio.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Кольцо прогресса дня: белая дуга по полупрозрачной дорожке, внутри число и подпись. Рисуется на градиентной плитке. */
@Composable
fun StudyRing(progress: Float, value: String, caption: String, modifier: Modifier = Modifier) {
    val sweep by animateFloatAsState(progress.coerceIn(0f, 1f), tween(700), label = "ring")
    Box(modifier.size(98.dp), Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val inset = 6.dp.toPx()
            val box = Size(size.width - 2 * inset, size.height - 2 * inset)
            val stroke = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
            drawArc(Color.White.copy(alpha = 0.25f), -90f, 360f, false, Offset(inset, inset), box, style = stroke)
            if (sweep > 0f) drawArc(Color.White, -90f, 360f * sweep, false, Offset(inset, inset), box, style = stroke)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.titleLarge, color = Color.White)
            FitText(caption.capFirst(), MaterialTheme.typography.labelMedium, Modifier.widthIn(max = 64.dp), Color.White.copy(alpha = 0.8f), 8.sp)
        }
    }
}
