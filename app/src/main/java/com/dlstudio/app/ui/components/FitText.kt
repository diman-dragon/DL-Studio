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
 * Межстрочный интервал уменьшается вместе с кеглем, чтобы пропорция строки не менялась.
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
    val line = if (style.lineHeight.isSp) (style.lineHeight.value * size.value / style.fontSize.value).sp else style.lineHeight
    Text(
        text, modifier, color, size, lineHeight = line, style = style, maxLines = maxLines, softWrap = maxLines > 1,
        onTextLayout = { r ->
            val cut = (0 until r.lineCount - 1).any { i ->
                val e = r.getLineEnd(i, true)
                e in 1 until text.length && !text[e - 1].isWhitespace() && !text[e].isWhitespace() && text[e - 1] != '-'
            }
            if ((r.didOverflowWidth || r.didOverflowHeight || cut) && size > minSize) size = (size.value * 0.92f).coerceAtLeast(minSize.value).sp
        }
    )
}
