package com.dlstudio.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

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
