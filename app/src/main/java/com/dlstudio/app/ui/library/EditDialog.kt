package com.dlstudio.app.ui.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dlstudio.app.library.LibNode
import com.dlstudio.app.ui.theme.LocalStrings

/** Создание/правка: название (у карточки — лицевая сторона) и, для карточки, обратная сторона. */
@Composable
fun EditDialog(level: Int, node: LibNode?, onSave: (String, String) -> Unit, onDismiss: () -> Unit) {
    val s = LocalStrings.current
    var name by remember { mutableStateOf(node?.name.orEmpty()) }
    var back by remember { mutableStateOf(node?.back.orEmpty()) }
    val wide = Modifier.fillMaxWidth()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (node == null) s.addTitle(level) else s.editTitle(level)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(name, { name = it }, wide, singleLine = level < 2, label = { Text(s.nameLabel(level)) })
                if (level == 2) OutlinedTextField(back, { back = it }, wide, label = { Text(s.cardBack) })
            }
        },
        confirmButton = { TextButton({ onSave(name.trim(), back.trim()) }, enabled = name.isNotBlank()) { Text(s.save) } },
        dismissButton = { TextButton(onDismiss) { Text(s.cancel) } }
    )
}
