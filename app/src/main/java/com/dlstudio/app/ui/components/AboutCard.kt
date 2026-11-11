package com.dlstudio.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dlstudio.app.BuildConfig
import com.dlstudio.app.ui.theme.LocalStrings

/** Постоянная карточка о приложении и компании в настройках. */
@Composable
fun AboutCard() {
    val s = LocalStrings.current
    Surface(Modifier.fillMaxWidth(), MaterialTheme.shapes.large, MaterialTheme.colorScheme.surfaceVariant, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Column(Modifier.padding(18.dp), Arrangement.spacedBy(6.dp)) {
            Text(s.aboutTitle, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
            Text("DataLaw DesignLab", style = MaterialTheme.typography.titleMedium)
            Text(s.aboutText, style = MaterialTheme.typography.bodyMedium)
            Text("v${BuildConfig.VERSION_NAME} · ${s.aboutCopyright}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
