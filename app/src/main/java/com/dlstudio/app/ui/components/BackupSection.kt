package com.dlstudio.app.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dlstudio.app.ui.theme.LocalStrings

private const val FILE_NAME = "dlstudio-progress.json"

/** Резервная копия прогресса: «Сохранить» — в файл, «Загрузить» — из файла (после подтверждения: текущий прогресс заменяется). */
@Composable
fun BackupSection(onSave: (Uri) -> Boolean, onRestore: (Uri) -> Boolean) {
    val s = LocalStrings.current
    var note by remember { mutableStateOf<String?>(null) }
    var picked by remember { mutableStateOf<Uri?>(null) }
    val saver = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) note = if (onSave(uri)) s.backupSaved else s.backupFailed
    }
    val loader = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> if (uri != null) picked = uri }
    SettingsSection(s.sectionBackup) {
        SettingRow {
            ActionTile(s.backupSave, { saver.launch(FILE_NAME) }, Modifier.weight(1f))
            ActionTile(s.backupLoad, { loader.launch(arrayOf("*/*")) }, Modifier.weight(1f))
        }
        Text(note ?: s.backupHint, Modifier.padding(horizontal = 6.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    picked?.let { uri ->
        DlModal.Shell(onDismiss = { picked = null }) {
            Text(s.backupQuestion, style = MaterialTheme.typography.bodyLarge)
            DlModal.Actions(s.cancel, s.backupLoad, { picked = null }, { note = if (onRestore(uri)) s.backupRestored else s.backupBad; picked = null }, danger = true)
        }
    }
}
