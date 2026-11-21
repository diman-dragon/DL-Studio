package com.dlstudio.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp

/**
 * Единые правила набора для любого стиля: вес; перенос только по границам слов, а длинные слова в обычном тексте —
 * по правилам языка (Hyphens.Auto; в заголовках отключено). Заголовки балансируются (Heading). Межстрочный интервал —
 * `fontSize × ratio`; лишнее место сверху и снизу строки срезано, поэтому текст в кнопках стоит ровно по центру.
 */
fun TextStyle.dlStyle(weight: FontWeight, heading: Boolean = false, ratio: Float = if (heading) 1.2f else 1.32f) = copy(
    fontWeight = weight,
    hyphens = if (heading) Hyphens.None else Hyphens.Auto,
    lineBreak = if (heading) LineBreak.Heading else LineBreak.Paragraph,
    lineHeight = (fontSize.value * ratio).sp,
    lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.Both)
)

/** Типографика интерфейса: системный шрифт, везде безопасные переносы. */
fun dlTypography(): Typography = Typography().let { b ->
    Typography(
        displayLarge = b.displayLarge.dlStyle(FontWeight.Bold, heading = true),
        displayMedium = b.displayMedium.dlStyle(FontWeight.Bold, heading = true),
        displaySmall = b.displaySmall.dlStyle(FontWeight.Bold, heading = true),
        headlineLarge = b.headlineLarge.dlStyle(FontWeight.Bold, heading = true),
        headlineMedium = b.headlineMedium.dlStyle(FontWeight.Bold, heading = true),
        headlineSmall = b.headlineSmall.dlStyle(FontWeight.SemiBold, heading = true),
        titleLarge = b.titleLarge.dlStyle(FontWeight.SemiBold, heading = true),
        titleMedium = b.titleMedium.dlStyle(FontWeight.SemiBold, heading = true),
        titleSmall = b.titleSmall.dlStyle(FontWeight.SemiBold, heading = true),
        bodyLarge = b.bodyLarge.dlStyle(FontWeight.Normal),
        bodyMedium = b.bodyMedium.dlStyle(FontWeight.Normal),
        bodySmall = b.bodySmall.dlStyle(FontWeight.Normal),
        labelLarge = b.labelLarge.dlStyle(FontWeight.Medium, ratio = 1.2f),
        labelMedium = b.labelMedium.dlStyle(FontWeight.Medium, ratio = 1.2f),
        labelSmall = b.labelSmall.dlStyle(FontWeight.Medium, ratio = 1.2f)
    )
}
