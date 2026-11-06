package com.dlstudio.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

/** Тема + типографика. Масштаб шрифта системы ограничен 1.15, чтобы вёрстка не разваливалась. */
@Composable
fun DlTheme(dark: Boolean, content: @Composable () -> Unit) {
    val density = LocalDensity.current
    val capped = Density(density.density, density.fontScale.coerceAtMost(1.15f))
    CompositionLocalProvider(LocalDensity provides capped) {
        MaterialTheme(colorScheme = if (dark) AmoledColors else LightColors, typography = DlTypography) {
            SystemBars(dark)
            content()
        }
    }
}
