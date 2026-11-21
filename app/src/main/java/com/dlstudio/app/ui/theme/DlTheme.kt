package com.dlstudio.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Density
import androidx.core.view.WindowCompat
import com.dlstudio.app.settings.AccentColor

/** Тема (цвета с акцентом). Масштаб шрифта системы ограничен 1.15, чтобы вёрстка не разваливалась. */
@Composable
fun DlTheme(dark: Boolean, accent: AccentColor, content: @Composable () -> Unit) {
    val system = LocalDensity.current
    val density = remember(system) { Density(system.density, system.fontScale.coerceAtMost(1.15f)) }
    val typography = remember { dlTypography() }
    CompositionLocalProvider(LocalDensity provides density, LocalAccent provides accent) {
        MaterialTheme(colorScheme = accentScheme(dark, accent), typography = typography) {
            SystemBars(dark)
            content()
        }
    }
}

/** Статус-бар и панель навигации в цвет фона; значки светлые на тёмной теме и тёмные на светлой. */
@Composable
fun SystemBars(dark: Boolean) {
    val view = LocalView.current
    val color = MaterialTheme.colorScheme.background.toArgb()
    SideEffect {
        val window = (view.context as Activity).window
        window.statusBarColor = color
        window.navigationBarColor = color
        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = !dark
            isAppearanceLightNavigationBars = !dark
        }
    }
}
