package com.dlstudio.app.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

/** Транскрипция под словом; если она записана без скобок, берётся в косые черты: `kæt` → `/kæt/`. */
@Composable
fun IpaText(ipa: String, modifier: Modifier = Modifier) {
    val shown = if (ipa.first() == '/' || ipa.first() == '[') ipa else "/$ipa/"
    FitText(shown, MaterialTheme.typography.titleLarge.copy(fontSize = 26.sp, textAlign = TextAlign.Center), modifier, MaterialTheme.colorScheme.onSurfaceVariant, 14.sp)
}
