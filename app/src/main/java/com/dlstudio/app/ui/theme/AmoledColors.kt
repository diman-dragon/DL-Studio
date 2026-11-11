package com.dlstudio.app.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

/** Тёмная тема: настоящий чёрный фон (AMOLED), поверхности — на ступень светлее, тонкие контуры. */
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
    onBackground = Color(0xFFECE8FA),
    onSurface = Color(0xFFECE8FA),
    onSurfaceVariant = Color(0xFFB9B4CC),
    outline = Color(0xFF2C2C36),
    outlineVariant = Color(0xFF1E1E26),
    error = Color(0xFFFF6B7A)
)
