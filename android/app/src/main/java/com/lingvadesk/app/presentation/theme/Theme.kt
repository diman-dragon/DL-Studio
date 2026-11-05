package com.lingvadesk.app.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

private val Paper = lightColorScheme(
    primary = Color(0xFF2F6F5E), onPrimary = Color(0xFFFFFFFF),
    background = Color(0xFFF8F0E4), onBackground = Color(0xFF2A241C),
    surface = Color(0xFFFFFAF0), onSurface = Color(0xFF2A241C),
    surfaceVariant = Color(0xFFEFE3D0), onSurfaceVariant = Color(0xFF4A4032),
    error = Color(0xFFB3261E)
)

/** Чёрная AMOLED. */
private val Amoled = darkColorScheme(
    primary = Color(0xFF7FD1B9), onPrimary = Color(0xFF00261C),
    background = Color(0xFF000000), onBackground = Color(0xFFEDE6DA),
    surface = Color(0xFF0E0E0E), onSurface = Color(0xFFEDE6DA),
    surfaceVariant = Color(0xFF1C1C1C), onSurfaceVariant = Color(0xFFD5CCBD),
    error = Color(0xFFFFB4AB)
)

/** theme: auto | light | dark; font: s | m | l. */
@Composable
fun LingvaTheme(theme: String, font: String, content: @Composable () -> Unit) {
    val dark = when (theme) { "dark" -> true; "light" -> false; else -> isSystemInDarkTheme() }
    val factor = when (font) { "s" -> 0.9f; "l" -> 1.2f; else -> 1f }
    val d = LocalDensity.current
    CompositionLocalProvider(LocalDensity provides Density(d.density, d.fontScale * factor)) {
        MaterialTheme(colorScheme = if (dark) Amoled else Paper, content = content)
    }
}
