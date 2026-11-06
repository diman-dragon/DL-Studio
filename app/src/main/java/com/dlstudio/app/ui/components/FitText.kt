package com.dlstudio.app.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * Текст, который сам уменьшается, пока не поместится: ни обрезков, ни разрыва слова посередине.
 * В одну строку слово не переносится вовсе; в несколько — переносы только по правилам языка.
 */
@Composable
fun FitText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    minSize: TextUnit = 10.sp,
    maxLines: Int = 1
) {
    var size by remember(text, style, maxLines) { mutableStateOf(style.fontSize) }
    Text(
        text, modifier, color, size, style = style, maxLines = maxLines, softWrap = maxLines > 1,
        onTextLayout = {
            if ((it.didOverflowWidth || it.didOverflowHeight) && size > minSize) size = (size.value * 0.92f).coerceAtLeast(minSize.value).sp
        }
    )
}
