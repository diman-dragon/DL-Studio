package com.dlstudio.app.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import com.dlstudio.app.settings.AccentColor

/** Выбранный акцентный цвет для любого экрана (градиенты кнопок и плиток). */
val LocalAccent = staticCompositionLocalOf { AccentColor.VIOLET }
