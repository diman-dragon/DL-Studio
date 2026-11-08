package com.dlstudio.app.ui.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit

/**
 * Текст по центру области, который занимает её максимально: кегль подбирается от `maxSize` до `minSize` так,
 * чтобы всё поместилось (по ширине и высоте области, не больше `maxLines` строк) и слова не рвались.
 * Нужна ограниченная по ширине область; по высоте — желательно тоже. Внутри `IntrinsicSize` не использовать.
 */
@Composable
fun BigText(text: String, style: TextStyle, modifier: Modifier = Modifier, color: Color = Color.Unspecified, maxSize: TextUnit, minSize: TextUnit, maxLines: Int = 2) {
    val measurer = rememberTextMeasurer()
    BoxWithConstraints(modifier, contentAlignment = Alignment.Center) {
        val width = constraints.maxWidth
        val height = if (constraints.hasBoundedHeight) constraints.maxHeight else Int.MAX_VALUE
        val size = remember(text, style, width, height, maxSize, minSize, maxLines) {
            TextFit.size(measurer, text, style, width, height, maxSize.value, minSize.value, maxLines)
        }
        Text(text, color = color, style = TextFit.styled(style, size).copy(textAlign = TextAlign.Center), maxLines = maxLines, softWrap = true)
    }
}
