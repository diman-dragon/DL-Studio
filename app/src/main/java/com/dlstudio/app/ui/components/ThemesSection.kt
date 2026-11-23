package com.dlstudio.app.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    var removing by remember { mutableStateOf<List<Topic>>(emptyList()) }
    var removingAll by remember { mutableStateOf(false) }
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
            val byId = available.associateBy { it.id }
            TopicTree(
                "available", available.map { TreeRow(it.id, it.course, it.group, it.title, it.id in state.busy) }, s.download, s.download,
                { row -> byId[row.id]?.let { vm.download(listOf(it)) } }, { rows -> vm.download(rows.mapNotNull { byId[it.id] }) }
            )
        }
        if (state.topics.isNotEmpty()) {
            Text(s.installedTitle, Modifier.padding(start = 6.dp, top = 4.dp), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            ActionTile(s.removeAll, { removingAll = true })
            val byId = state.topics.associateBy { it.id }
            TopicTree(
                "installed", state.topics.map { TreeRow(it.id, it.course, it.group, it.title) }, s.remove, s.remove,
                { row -> byId[row.id]?.let { removing = listOf(it) } }, { rows -> removing = rows.mapNotNull { byId[it.id] } }
            )
        }
    }
    if (editing) CatalogDialog(state.catalogUrl, { editing = false }) { vm.setCatalogUrl(it); editing = false }
    if (removingAll) {
        DlModal.Shell(onDismiss = { removingAll = false }) {
            Text(s.removeAllQuestion(state.topics.size), style = MaterialTheme.typography.bodyLarge)
            DlModal.Actions(s.cancel, s.removeAll, { removingAll = false }, { vm.removeAll(); removingAll = false }, danger = true)
        }
    }
    if (removing.isNotEmpty()) {
        DlModal.Shell(onDismiss = { removing = emptyList() }) {
            Text(if (removing.size == 1) s.removeQuestion(removing[0].title) else s.removeManyQuestion(removing.size), style = MaterialTheme.typography.bodyLarge)
            DlModal.Actions(s.cancel, s.remove, { removing = emptyList() }, { vm.removeMany(removing); removing = emptyList() }, danger = true)
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
