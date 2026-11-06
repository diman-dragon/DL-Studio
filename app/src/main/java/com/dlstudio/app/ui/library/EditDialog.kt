package com.dlstudio.app.ui.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dlstudio.app.library.LibNode
import com.dlstudio.app.ui.theme.LocalStrings
import com.dlstudio.app.ui.components.DlAction
import com.dlstudio.app.ui.components.DlModal

/** Создание/правка: название (у карточки — лицевая сторона) и, для карточки, обратная сторона. */
@Composable
fun EditDialog(level: Int, node: LibNode?, onSave: (String, String, String, String, String) -> Unit, onDismiss: () -> Unit) {
    val s = LocalStrings.current
    var name by remember { mutableStateOf(node?.name.orEmpty()) }
    var back by remember { mutableStateOf(node?.back.orEmpty()) }
    var hint by remember { mutableStateOf(node?.hint.orEmpty()) }
    var explanation by remember { mutableStateOf(node?.explanation.orEmpty()) }
    var example by remember { mutableStateOf(node?.example.orEmpty()) }
    val wide = Modifier.fillMaxWidth()
    DlModal(if (node == null) s.addTitle(level) else s.editTitle(level), onDismiss) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(name, { name = it }, wide, singleLine = level < 2, label = { Text(s.nameLabel(level)) })
                if (level == 2) OutlinedTextField(back, { back = it }, wide, label = { Text(s.cardBack) })
                if (level == 2) OutlinedTextField(hint, { hint = it }, wide, label = { Text(s.cardHint) })
                if (level == 2) OutlinedTextField(explanation, { explanation = it }, wide, label = { Text(s.cardExplanation) })
                if (level == 2) OutlinedTextField(example, { example = it }, wide, label = { Text(s.cardExample) })
            }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            DlAction(s.cancel, onDismiss, outlined = true)
            Spacer(Modifier.width(8.dp))
            DlAction(s.save, { onSave(name.trim(), back.trim(), hint.trim(), explanation.trim(), example.trim()) }, enabled = name.isNotBlank())
        }
    }
}
