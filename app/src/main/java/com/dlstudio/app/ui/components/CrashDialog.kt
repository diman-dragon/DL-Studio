package com.dlstudio.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dlstudio.app.ui.theme.LocalStrings

private const val MAX_HEIGHT_DP = 280

/** Показывается при запуске после падения: текст ошибки (прокручивается), «Скопировать» и «Закрыть» (после закрытия отчёт стирается). */
@Composable
fun CrashDialog(report: String, onClose: () -> Unit) {
    val s = LocalStrings.current
    val clipboard = LocalClipboardManager.current
    DlModal.Shell(s.crashTitle, onDismiss = onClose) {
        Text(s.crashHint, style = MaterialTheme.typography.bodyMedium)
        Column(Modifier.heightIn(max = MAX_HEIGHT_DP.dp).verticalScroll(rememberScrollState())) {
            Text(report, fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        DlModal.Actions(s.close, s.copy, onClose, { clipboard.setText(AnnotatedString(report)) })
    }
}
