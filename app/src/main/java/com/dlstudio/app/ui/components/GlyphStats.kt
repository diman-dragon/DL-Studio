package com.dlstudio.app.ui.components

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope

/** Значок «Статистика»: три растущих столбика. */
fun DrawScope.glyphStats(color: Color) {
    val w = size.width
    val bar = w * 0.14f
    listOf(0.22f to 0.22f, 0.43f to 0.36f, 0.64f to 0.52f).forEach { (x, height) ->
        drawRoundRect(color = color, topLeft = Offset(w * x, w * (0.78f - height)), size = Size(bar, w * height), cornerRadius = CornerRadius(bar / 3))
    }
}
