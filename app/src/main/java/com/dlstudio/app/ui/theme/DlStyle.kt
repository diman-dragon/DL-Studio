package com.dlstudio.app.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.LineBreak

/**
 * Единые правила набора для любого стиля: наш шрифт и вес; перенос только по границам слов,
 * а длинные слова в обычном тексте — по правилам языка (Hyphens.Auto; в заголовках отключено). Заголовки балансируются (Heading).
 */
fun TextStyle.dlStyle(weight: FontWeight, family: FontFamily = DlFonts, heading: Boolean = false) = copy(
    fontFamily = family,
    fontWeight = weight,
    hyphens = if (heading) Hyphens.None else Hyphens.Auto,
    lineBreak = if (heading) LineBreak.Heading else LineBreak.Paragraph
)
