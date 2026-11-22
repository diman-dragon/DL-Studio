package com.dlstudio.app.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dlstudio.app.ui.theme.LocalStrings

/** Раздел «Данные» в настройках: сброс прогресса с подтверждением. Темы и настройки не затрагиваются. */
@Composable
fun ResetSection(onReset: () -> Unit) {
    val s = LocalStrings.current
    var asking by remember { mutableStateOf(false) }
    SettingsSection(s.sectionData) {
        SettingCard(onClick = { asking = true }, gap = 3.dp) {
            Text(s.resetProgress, Modifier.fillMaxWidth(), MaterialTheme.colorScheme.error, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        }
        Text(s.resetProgressHint, Modifier.padding(horizontal = 6.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    if (asking) {
        DlModal.Shell(onDismiss = { asking = false }) {
            Text(s.resetQuestion, style = MaterialTheme.typography.bodyLarge)
            DlModal.Actions(s.cancel, s.reset, { asking = false }, { onReset(); asking = false }, danger = true)
        }
    }
}
