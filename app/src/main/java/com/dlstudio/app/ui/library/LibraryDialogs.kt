package com.dlstudio.app.ui.library

import androidx.compose.runtime.Composable
import com.dlstudio.app.library.LibraryViewModel
import com.dlstudio.app.ui.theme.LocalStrings

/** Показывает нужный диалог по `dialog` и применяет результат к библиотеке. */
@Composable
fun LibraryDialogs(dialog: LibDialog?, level: Int, vm: LibraryViewModel, onClose: () -> Unit) {
    val node = dialog?.node
    when {
        dialog == null -> Unit
        dialog.delete && node != null -> ConfirmDialog(
            LocalStrings.current.deleteQuestion(level, node.name),
            { vm.remove(node.id); onClose() },
            onClose
        )
        else -> EditDialog(level, node, { name, back, hint, explanation, example ->
            if (node == null) vm.add(name, back, hint, explanation, example)
            else vm.edit(node.id, name, back, hint, explanation, example)
            onClose()
        }, onClose)
    }
}
