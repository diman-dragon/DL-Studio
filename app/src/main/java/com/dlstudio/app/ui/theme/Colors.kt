package com.dlstudio.app.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
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
    val amber = Color(if (dark) 0xFFFFC857 else 0xFF9A4A00)
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

/** Тёмная тема: настоящий чёрный фон (AMOLED), поверхности — на ступень светлее, тонкие контуры. Текст почти белый: контраст с фоном выше 12:1. */
val AmoledColors = darkColorScheme(
    primary = Color(0xFFB69CFF),
    onPrimary = Color(0xFF1E0A5A),
    background = Color.Black,
    surface = Color.Black,
    surfaceTint = Color.Transparent,
    surfaceVariant = Color(0xFF131318),
    surfaceContainerLow = Color(0xFF0D0D11),
    surfaceContainerHigh = Color(0xFF202027),
    surfaceContainerHighest = Color(0xFF2A2932),
    onBackground = Color(0xFFF7F5FF),
    onSurface = Color(0xFFF7F5FF),
    onSurfaceVariant = Color(0xFFD6D2E6),
    outline = Color(0xFF3A3A47),
    outlineVariant = Color(0xFF2A2A34),
    error = Color(0xFFFF6B7A)
)

/** Светлая тема. */
val LightColors = lightColorScheme(
    primary = Color(0xFF5B3FD6),
    onPrimary = Color.White,
    background = Color(0xFFF7F5FF),
    surface = Color(0xFFFFFFFF),
    surfaceTint = Color.Transparent,
    surfaceVariant = Color(0xFFEAE6FA),
    surfaceContainerLow = Color(0xFFF1EEFA),
    surfaceContainerHigh = Color(0xFFE3DFF0),
    surfaceContainerHighest = Color(0xFFDAD5E8),
    onBackground = Color(0xFF1B1530),
    onSurface = Color(0xFF1B1530),
    onSurfaceVariant = Color(0xFF4A4368),
    outline = Color(0xFFD3CCEC),
    outlineVariant = Color(0xFFE2DDF4),
    error = Color(0xFFC62828)
)

/** Два цвета градиента акцента: от яркого к глубокому. */
fun AccentColor.gradientColors(): List<Color> = listOf(Color(main), Color(deep))
