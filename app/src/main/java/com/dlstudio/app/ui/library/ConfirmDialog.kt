package com.dlstudio.app.ui.library

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.dlstudio.app.ui.components.DlModal
import com.dlstudio.app.ui.components.DlModalActions
import com.dlstudio.app.ui.theme.LocalStrings

/** Подтверждение удаления: красная кнопка «Удалить» и «Отмена». */
@Composable
fun ConfirmDialog(text: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    val s = LocalStrings.current
    DlModal(s.delete, onDismiss) {
        Text(text)
        DlModalActions(s.cancel, s.delete, onDismiss, onConfirm, danger = true)
    }
}
