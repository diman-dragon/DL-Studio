package com.dlstudio.app.ui.library

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.dlstudio.app.library.ImportReport
import com.dlstudio.app.ui.theme.LocalStrings

/** Итог импорта: сколько карточек добавлено или что в файле ничего не нашлось. */
@Composable
fun ImportReportDialog(report: ImportReport, onClose: () -> Unit) {
    val s = LocalStrings.current
    AlertDialog(
        onDismissRequest = onClose,
        text = { Text(if (report.cards > 0) s.importDone(report.cards) else s.importEmpty) },
        confirmButton = { TextButton(onClose) { Text(s.close) } }
    )
}
