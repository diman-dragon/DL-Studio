package com.dlstudio.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.clickable
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

/** Раздел настроек: небольшой акцентный заголовок и плитки под ним. */
@Composable
fun SettingsSection(title: String, collapsible: Boolean = false, summary: String = "", content: @Composable ColumnScope.() -> Unit) {
    var expanded by rememberSaveable(title) { mutableStateOf(!collapsible) }
    val colors = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (collapsible) {
            Surface(Modifier.fillMaxWidth().clickable(role = Role.Button) { expanded = !expanded }, color = colors.surfaceVariant, shape = MaterialTheme.shapes.large) {
                Row(Modifier.padding(horizontal = 16.dp, vertical = 13.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(title, style = MaterialTheme.typography.titleMedium, color = colors.onSurface)
                        Text(summary, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                    }
                    Text(if (expanded) "−" else "+", Modifier.padding(start = 12.dp), style = MaterialTheme.typography.titleLarge, color = colors.primary)
                }
            }
        } else Text(title.uppercase(), Modifier.padding(start = 6.dp, top = 8.dp), style = MaterialTheme.typography.labelMedium, color = colors.primary)
        if (expanded) content()
    }
}
