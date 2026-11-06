package com.dlstudio.app.ui.library

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Arrangement
import com.dlstudio.app.library.ImportReport
import com.dlstudio.app.ui.components.DlAction
import com.dlstudio.app.ui.components.DlModal
import com.dlstudio.app.ui.theme.LocalStrings

/** Итог импорта: сколько карточек добавлено или что в файле ничего не нашлось. */
@Composable
fun ImportReportDialog(report: ImportReport, onClose: () -> Unit) {
    val s = LocalStrings.current
    DlModal(onDismiss = onClose) {
        Text(if (report.cards > 0) s.importDone(report.cards) else s.importEmpty)
        Row(androidx.compose.ui.Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { DlAction(s.close, onClose) }
    }
}
