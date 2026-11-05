package com.dlstudio.app.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

/** Тёмная тема: настоящий чёрный фон (AMOLED), без тонировки поверхностей. */
val AmoledColors = darkColorScheme(
    primary = Color(0xFFB69CFF),
    onPrimary = Color(0xFF1E0A5A),
    background = Color.Black,
    surface = Color.Black,
    surfaceTint = Color.Transparent,
    surfaceVariant = Color(0xFF161616),
    onBackground = Color(0xFFECE8FA),
    onSurface = Color(0xFFECE8FA),
    onSurfaceVariant = Color(0xFFCFC9E3)
)
