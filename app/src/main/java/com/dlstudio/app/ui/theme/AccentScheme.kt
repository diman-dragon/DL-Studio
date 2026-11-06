package com.dlstudio.app.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import com.dlstudio.app.settings.AccentColor

/** Цвета темы с выбранным акцентом: на чёрном — светлый оттенок, на светлом фоне — насыщенный. */
fun accentScheme(dark: Boolean, accent: AccentColor): ColorScheme =
    if (dark) AmoledColors.copy(primary = Color(accent.soft), onPrimary = Color(0xFF0B0B14))
    else LightColors.copy(primary = Color(accent.deep), onPrimary = Color.White)
