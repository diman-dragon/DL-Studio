package com.dlstudio.app.ui.components

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate

/** Значок «Библиотека»: две книги на полке и одна наклонённая. */
fun DrawScope.glyphLibrary(color: Color) {
    val w = size.width
    val corner = CornerRadius(w * 0.03f)
    drawRoundRect(color = color, topLeft = Offset(w * 0.20f, w * 0.24f), size = Size(w * 0.14f, w * 0.54f), cornerRadius = corner)
    drawRoundRect(color = color.copy(alpha = 0.7f), topLeft = Offset(w * 0.38f, w * 0.30f), size = Size(w * 0.14f, w * 0.48f), cornerRadius = corner)
    rotate(degrees = -18f, pivot = Offset(w * 0.64f, w * 0.78f)) {
        drawRoundRect(color = color, topLeft = Offset(w * 0.58f, w * 0.26f), size = Size(w * 0.14f, w * 0.52f), cornerRadius = corner)
    }
}
