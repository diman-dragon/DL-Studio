package com.dlstudio.app.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate

/** Значок «Настройки»: шестерёнка — кольцо и восемь зубцов. */
fun DrawScope.glyphSettings(color: Color) {
    val w = size.width
    drawCircle(color = color, radius = w * 0.19f, style = Stroke(width = w * 0.09f))
    for (i in 0 until 8) {
        rotate(degrees = i * 45f) {
            drawLine(color = color, start = Offset(center.x, center.y - w * 0.27f), end = Offset(center.x, center.y - w * 0.36f), strokeWidth = w * 0.1f, cap = StrokeCap.Round)
        }
    }
}
