package com.dlstudio.app.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp

/**
 * Единые правила набора для любого стиля: наш шрифт и вес; перенос только по границам слов,
 * а длинные слова в обычном тексте — по правилам языка (Hyphens.Auto; в заголовках отключено). Заголовки балансируются (Heading).
 * Межстрочный интервал — `fontSize × ratio` (заголовки и кнопки плотнее, абзацы чуть свободнее); лишнее место сверху и снизу строки срезано,
 * поэтому текст в кнопках стоит ровно по центру.
 */
fun TextStyle.dlStyle(weight: FontWeight, family: FontFamily = DlFonts, heading: Boolean = false, ratio: Float = if (heading) 1.2f else 1.32f) = copy(
    fontFamily = family,
    fontWeight = weight,
    hyphens = if (heading) Hyphens.None else Hyphens.Auto,
    lineBreak = if (heading) LineBreak.Heading else LineBreak.Paragraph,
    lineHeight = (fontSize.value * ratio).sp,
    lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.Both)
)
