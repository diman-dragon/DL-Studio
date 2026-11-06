package com.dlstudio.app.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import com.dlstudio.app.settings.AccentColor

/**
 * Цвета темы с выбранным акцентом: на чёрном — светлый оттенок, на светлом фоне — насыщенный.
 * Контейнеры (подсказка, озвучка, выбранные чипы) подмешивают акцент к фону, поэтому всегда в тон.
 */
fun accentScheme(dark: Boolean, accent: AccentColor): ColorScheme {
    val base = if (dark) AmoledColors else LightColors
    val primary = Color(if (dark) accent.soft else accent.deep)
    val amber = Color(if (dark) 0xFFFFC857 else 0xFFB35400)
    return base.copy(
        primary = primary,
        onPrimary = if (dark) Color(0xFF0B0B14) else Color.White,
        primaryContainer = lerp(base.surface, primary, if (dark) 0.22f else 0.14f),
        onPrimaryContainer = primary,
        tertiary = amber,
        tertiaryContainer = lerp(base.surface, amber, if (dark) 0.20f else 0.14f),
        onTertiaryContainer = amber
    )
}
