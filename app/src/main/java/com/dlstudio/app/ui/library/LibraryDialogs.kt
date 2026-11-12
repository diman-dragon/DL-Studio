package com.dlstudio.app.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.dlstudio.app.library.ImportReport
import com.dlstudio.app.library.LibNode
import com.dlstudio.app.library.LibraryViewModel
import com.dlstudio.app.settings.FontChoice
import com.dlstudio.app.ui.components.DlModal
import com.dlstudio.app.ui.theme.LocalStrings
import com.dlstudio.app.ui.theme.family

/** Открытый диалог: `node` null — создание; `delete` — подтверждение удаления, иначе правка. */
data class LibDialog(val node: LibNode?, val delete: Boolean = false)

/** Подтверждение удаления: красная кнопка «Удалить» и «Отмена». */
@Composable
fun ConfirmDialog(text: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    val s = LocalStrings.current
    DlModal.Shell(s.delete, onDismiss) {
        Text(text)
        DlModal.Actions(s.cancel, s.delete, onDismiss, onConfirm, danger = true)
    }
}

/** Создание/правка: название (у карточки — лицевая сторона) и, для карточки, обратная сторона, транскрипция, подсказка, пояснение и пример. Значения слова (`meanings`) сохраняются как есть. */
@Composable
fun EditDialog(level: Int, node: LibNode?, onSave: (LibNode) -> Unit, onDismiss: () -> Unit) {
    val s = LocalStrings.current
    var name by remember { mutableStateOf(node?.name.orEmpty()) }
    var back by remember { mutableStateOf(node?.back.orEmpty()) }
    var hint by remember { mutableStateOf(node?.hint.orEmpty()) }
    var explanation by remember { mutableStateOf(node?.explanation.orEmpty()) }
    var example by remember { mutableStateOf(node?.example.orEmpty()) }
    var ipa by remember { mutableStateOf(node?.ipa.orEmpty()) }
    val wide = Modifier.fillMaxWidth()
    DlModal.Shell(if (node == null) s.addTitle(level) else s.editTitle(level), onDismiss) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(name, { name = it }, wide, singleLine = level < 2, label = { Text(s.nameLabel(level)) })
                if (level == 2) OutlinedTextField(back, { back = it }, wide, label = { Text(s.cardBack) })
                if (level == 2) OutlinedTextField(ipa, { ipa = it }, wide, singleLine = true, label = { Text(s.cardIpa) })
                if (level == 2) OutlinedTextField(hint, { hint = it }, wide, label = { Text(s.cardHint) })
                if (level == 2) OutlinedTextField(explanation, { explanation = it }, wide, label = { Text(s.cardExplanation) })
                if (level == 2) OutlinedTextField(example, { example = it }, wide, label = { Text(s.cardExample) })
            }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            DlModal.Action(s.cancel, onDismiss, outlined = true)
            Spacer(Modifier.width(8.dp))
            DlModal.Action(s.save, { onSave((node ?: LibNode("", "")).copy(name = name.trim(), back = back.trim(), hint = hint.trim(), explanation = explanation.trim(), example = example.trim(), ipa = ipa.trim())) }, enabled = name.isNotBlank())
        }
    }
}

/** Подсказка перед импортом: что получится на этом уровне, какой формат нужен и пример; «Выбрать файл» открывает выбор. */
@Composable
fun ImportDialog(level: Int, onPick: () -> Unit, onDismiss: () -> Unit) {
    val s = LocalStrings.current
    val colors = MaterialTheme.colorScheme
    DlModal.Shell(s.importTitle(level), onDismiss) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(s.importOutcome(level))
                Text(s.importFormat, color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                Text(
                    "cat;кот\ndog;собака\nhouse;дом",
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(colors.surfaceVariant).padding(12.dp),
                    fontFamily = FontChoice.MONO.family()
                )
            }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            DlModal.Action(s.cancel, onDismiss, outlined = true)
            Spacer(Modifier.width(8.dp))
            DlModal.Action(s.importPick, onPick)
        }
    }
}

/** Итог импорта: сколько карточек добавлено или что в файле ничего не нашлось. */
@Composable
fun ImportReportDialog(report: ImportReport, onClose: () -> Unit) {
    val s = LocalStrings.current
    DlModal.Shell(onDismiss = onClose) {
        Text(if (report.cards > 0) s.importDone(report.cards) else s.importEmpty)
        Row(androidx.compose.ui.Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { DlModal.Action(s.close, onClose) }
    }
}

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
        else -> EditDialog(level, node, { draft ->
            if (node == null) vm.add(draft) else vm.edit(draft)
            onClose()
        }, onClose)
    }
}
