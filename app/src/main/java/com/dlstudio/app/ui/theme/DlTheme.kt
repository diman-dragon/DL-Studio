package com.dlstudio.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import com.dlstudio.app.settings.AccentColor

/** Тема (цвета с акцентом) + типографика. Масштаб шрифта системы ограничен 1.15, чтобы вёрстка не разваливалась. */
@Composable
fun DlTheme(dark: Boolean, accent: AccentColor, content: @Composable () -> Unit) {
    val density = LocalDensity.current
    CompositionLocalProvider(LocalDensity provides density, LocalAccent provides accent) {
        MaterialTheme(colorScheme = accentScheme(dark, accent), typography = DlTypography) {
            SystemBars(dark)
            content()
        }
    }
}
