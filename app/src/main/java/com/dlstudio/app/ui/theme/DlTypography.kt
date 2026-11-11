package com.dlstudio.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontWeight

/** Типографика приложения на системных шрифтах; везде безопасные переносы. */
val DlTypography = Typography().let { b ->
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
