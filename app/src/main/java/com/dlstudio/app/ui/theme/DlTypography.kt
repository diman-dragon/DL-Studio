package com.dlstudio.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontWeight

/** Типографика приложения: заголовки Inter Display, остальное Inter; везде безопасные переносы. */
val DlTypography = Typography().let { b ->
    Typography(
        displayLarge = b.displayLarge.dlStyle(FontWeight.Bold, DlDisplayFonts, true),
        displayMedium = b.displayMedium.dlStyle(FontWeight.Bold, DlDisplayFonts, true),
        displaySmall = b.displaySmall.dlStyle(FontWeight.Bold, DlDisplayFonts, true),
        headlineLarge = b.headlineLarge.dlStyle(FontWeight.Bold, DlDisplayFonts, true),
        headlineMedium = b.headlineMedium.dlStyle(FontWeight.Bold, DlDisplayFonts, true),
        headlineSmall = b.headlineSmall.dlStyle(FontWeight.SemiBold, DlDisplayFonts, true),
        titleLarge = b.titleLarge.dlStyle(FontWeight.SemiBold, DlDisplayFonts, true),
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
