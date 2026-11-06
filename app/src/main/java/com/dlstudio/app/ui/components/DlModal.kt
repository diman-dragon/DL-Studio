package com.dlstudio.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

/** Единая оболочка модальных окон DL-Studio; системные AlertDialog не используются. */
object DlModal {
    @Composable fun Shell(title: String? = null, onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
        Dialog(onDismissRequest = onDismiss) {
            Surface(Modifier.fillMaxWidth().padding(8.dp), RoundedCornerShape(28.dp), MaterialTheme.colorScheme.surface, tonalElevation = 6.dp, shadowElevation = 12.dp, border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)) {
                Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (title != null) Text(title, style = MaterialTheme.typography.headlineSmall); content()
                }
            }
        }
    }

    @Composable fun Actions(cancel: String, confirm: String, onCancel: () -> Unit, onConfirm: () -> Unit, danger: Boolean = false) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
            Action(cancel, onCancel, outlined = true); Spacer(Modifier.width(8.dp)); Action(confirm, onConfirm, danger = danger)
        }
    }

    @Composable fun Action(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, outlined: Boolean = false, danger: Boolean = false, enabled: Boolean = true) {
        val color = if (danger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        Surface(modifier.clickable(enabled = enabled, role = Role.Button, onClick = onClick).semantics { contentDescription = label }, shape = RoundedCornerShape(15.dp), color = if (!enabled) MaterialTheme.colorScheme.surfaceVariant else if (outlined) Color.Transparent else color, tonalElevation = if (outlined) 0.dp else 2.dp, border = if (outlined) androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = .5f)) else null) {
            Text(label, Modifier.padding(horizontal = 16.dp, vertical = 11.dp), color = if (!enabled) MaterialTheme.colorScheme.onSurfaceVariant else if (outlined) color else MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.labelLarge)
        }
    }
}
