package com.dlstudio.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun DlTheme(dark: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (dark) AmoledColors else LightColors) {
        SystemBars(dark)
        content()
    }
}
