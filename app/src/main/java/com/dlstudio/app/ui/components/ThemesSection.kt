package com.dlstudio.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dlstudio.app.content.Topic
import com.dlstudio.app.ui.AppState
import com.dlstudio.app.ui.AppViewModel
import com.dlstudio.app.ui.Notice
import com.dlstudio.app.ui.theme.LocalStrings

/** Раздел «Темы» в настройках: адрес каталога, список доступных для загрузки, установленные темы, установка из файла. */
@Composable
fun ThemesSection(state: AppState, vm: AppViewModel, onPickFile: () -> Unit) {
    val s = LocalStrings.current
    var editing by remember { mutableStateOf(false) }
    var removing by remember { mutableStateOf<Topic?>(null) }
    val installed = state.topics.map { it.id }.toSet()
    val available = state.catalog.filter { it.id !in installed }

    SettingsSection(s.sectionTopics) {
        SettingTile(s.catalogAddress, state.catalogUrl.ifBlank { s.catalogNotSet }) { editing = true }
        SettingRow {
            ActionTile(s.refreshCatalog, vm::refreshCatalog, Modifier.weight(1f))
            ActionTile(s.fromFile, onPickFile, Modifier.weight(1f))
        }
        val status = when {
            state.catalogLoading -> s.catalogLoading
            state.catalogFailed -> s.catalogFailed
            state.notice == Notice.DOWNLOAD_FAILED -> s.downloadFailed
            state.notice == Notice.FILE_FAILED -> s.fileFailed
            state.catalog.isNotEmpty() && available.isEmpty() -> s.nothingNew
            else -> null
        }
        if (status != null) Text(status, Modifier.padding(horizontal = 6.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (available.isNotEmpty()) {
            Text(s.availableTitle, Modifier.padding(start = 6.dp, top = 4.dp), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            if (available.size > 1) ActionTile(s.downloadAll, { vm.download(available) })
            available.forEach { entry ->
                TopicRow(entry.title, listOf(entry.course, entry.group).filter { it.isNotBlank() }.joinToString(" · "), s.download, entry.id in state.busy) { vm.download(listOf(entry)) }
            }
        }
        if (state.topics.isNotEmpty()) {
            Text(s.installedTitle, Modifier.padding(start = 6.dp, top = 4.dp), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            state.topics.forEach { topic ->
                TopicRow(topic.title, listOf(topic.course, topic.group).filter { it.isNotBlank() }.joinToString(" · "), s.remove, false) { removing = topic }
            }
        }
    }
    if (editing) CatalogDialog(state.catalogUrl, { editing = false }) { vm.setCatalogUrl(it); editing = false }
    removing?.let { topic ->
        DlModal.Shell(onDismiss = { removing = null }) {
            Text(s.removeQuestion(topic.title), style = MaterialTheme.typography.bodyLarge)
            DlModal.Actions(s.cancel, s.remove, { removing = null }, { vm.remove(topic); removing = null }, danger = true)
        }
    }
}

/** Строка темы: название, подпись и действие справа (при загрузке вместо него крутится индикатор). */
@Composable
private fun TopicRow(title: String, subtitle: String, action: String, busy: Boolean, onClick: () -> Unit) {
    SettingCard(onClick = if (busy) null else onClick, gap = 2.dp) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Column(Modifier.weight(1f), Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, maxLines = 2)
                if (subtitle.isNotBlank()) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (busy) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
            else Text(action, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun CatalogDialog(current: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    val s = LocalStrings.current
    var text by remember { mutableStateOf(current) }
    DlModal.Shell(s.catalogAddress, onDismiss) {
        OutlinedTextField(text, { text = it }, Modifier.fillMaxWidth(), singleLine = true, placeholder = { Text("https://…") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri))
        Text(s.catalogHint, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        DlModal.Actions(s.cancel, s.save, onDismiss, { onSave(text) })
    }
}
