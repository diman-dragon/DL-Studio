package com.dlstudio.app.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.dlstudio.app.ui.theme.LocalStrings

/** Подсказка перед импортом: что получится на этом уровне, какой формат нужен и пример; «Выбрать файл» открывает выбор. */
@Composable
fun ImportDialog(level: Int, onPick: () -> Unit, onDismiss: () -> Unit) {
    val s = LocalStrings.current
    val colors = MaterialTheme.colorScheme
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(s.importTitle(level)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(s.importOutcome(level))
                Text(s.importFormat, color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                Text(
                    "cat;кот\ndog;собака\nhouse;дом",
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(colors.surfaceVariant).padding(12.dp),
                    fontFamily = FontFamily.Monospace
                )
            }
        },
        confirmButton = { TextButton(onPick) { Text(s.importPick) } },
        dismissButton = { TextButton(onDismiss) { Text(s.cancel) } }
    )
}
