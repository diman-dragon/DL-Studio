package com.dlstudio.app.ui.library

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.dlstudio.app.ui.theme.LocalStrings

/** Подтверждение удаления: красная кнопка «Удалить» и «Отмена». */
@Composable
fun ConfirmDialog(text: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    val s = LocalStrings.current
    AlertDialog(
        onDismissRequest = onDismiss,
        text = { Text(text) },
        confirmButton = { TextButton(onConfirm) { Text(s.delete, color = MaterialTheme.colorScheme.error) } },
        dismissButton = { TextButton(onDismiss) { Text(s.cancel) } }
    )
}
