package com.dlstudio.app.ui.components

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.sp

/** Подбор самого крупного кегля, при котором текст целиком помещается в область, а слова не рвутся посередине. */
object TextFit {
    /** Стиль с кеглем `size`; межстрочный интервал сохраняет пропорцию исходного стиля, переносов внутри слова нет. */
    fun styled(style: TextStyle, size: Float): TextStyle {
        val ratio = if (style.lineHeight.isSp && style.fontSize.value > 0f) style.lineHeight.value / style.fontSize.value else 1.2f
        return style.copy(fontSize = size.sp, lineHeight = (size * ratio).sp, hyphens = Hyphens.None)
    }

    /** Наибольший кегль от `min` до `max` (sp) для области `width` × `height` px; если не помещается и минимальный — вернёт `min`. */
    fun size(measurer: TextMeasurer, text: AnnotatedString, style: TextStyle, width: Int, height: Int, max: Float, min: Float, maxLines: Int): Float {
        if (fits(measurer, text, styled(style, max), width, height, maxLines)) return max
        var low = min
        var high = max
        while (high - low > 0.5f) {
            val mid = (low + high) / 2
            if (fits(measurer, text, styled(style, mid), width, height, maxLines)) low = mid else high = mid
        }
        return low
    }

    private fun fits(measurer: TextMeasurer, text: AnnotatedString, style: TextStyle, width: Int, height: Int, maxLines: Int): Boolean {
        val r = measurer.measure(text = text, style = style, maxLines = maxLines, constraints = Constraints(maxWidth = width))
        val cut = (0 until r.lineCount - 1).any { i ->
            val e = r.getLineEnd(i, true)
            val t = text.text
            e in 1 until t.length && !t[e - 1].isWhitespace() && !t[e].isWhitespace() && t[e - 1] != '-'
        }
        return !cut && !r.didOverflowHeight && !r.didOverflowWidth && r.size.height <= height
    }
}
