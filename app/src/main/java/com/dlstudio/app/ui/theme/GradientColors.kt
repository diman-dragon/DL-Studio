package com.dlstudio.app.ui.theme

import androidx.compose.ui.graphics.Color
import com.dlstudio.app.settings.AccentColor

/** Два цвета градиента акцента: от яркого к глубокому. */
fun AccentColor.gradientColors(): List<Color> = listOf(Color(main), Color(deep))
