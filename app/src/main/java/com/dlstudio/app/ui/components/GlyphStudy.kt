package com.dlstudio.app.ui.components

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope

/** Значок «Учёба»: две стопкой лежащие карточки. */
fun DrawScope.glyphStudy(color: Color) {
    val w = size.width
    val corner = CornerRadius(w * 0.08f)
    drawRoundRect(color = color.copy(alpha = 0.55f), topLeft = Offset(w * 0.30f, w * 0.20f), size = Size(w * 0.44f, w * 0.48f), cornerRadius = corner)
    drawRoundRect(color = color, topLeft = Offset(w * 0.24f, w * 0.30f), size = Size(w * 0.44f, w * 0.48f), cornerRadius = corner)
}
