package com.dlstudio.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * Подпись кнопки: область текста — 90% ширины кнопки (остальное — отступы по краям). Кегль начинается с `maxSize`
 * и сам уменьшается до `minSize`, пока слово не поместится без обрезки и разрыва; интервал между строками плотный (1.15).
 * Высоту области ограничивает вызывающий через `modifier` (например, `fillMaxHeight(0.9f)`).
 */
@Composable
fun ButtonLabel(text: String, color: Color, modifier: Modifier = Modifier, maxSize: TextUnit = 24.sp, minSize: TextUnit = 11.sp, maxLines: Int = 2) {
    val style = MaterialTheme.typography.titleMedium.copy(
        fontSize = maxSize, lineHeight = (maxSize.value * 1.15f).sp, textAlign = TextAlign.Center, fontWeight = FontWeight.SemiBold
    )
    Box(modifier.fillMaxWidth(0.9f), Alignment.Center) { FitText(text, style, Modifier.fillMaxWidth(), color, minSize, maxLines) }
}
