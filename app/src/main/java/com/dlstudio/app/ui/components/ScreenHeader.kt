package com.dlstudio.app.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Заголовок экрана: одна строка, при нехватке места шрифт уменьшается (слово не рвётся). */
@Composable
fun ScreenHeader(text: String) {
    FitText(text, MaterialTheme.typography.headlineMedium, Modifier.fillMaxWidth().padding(bottom = 16.dp), minSize = 20.sp)
}
