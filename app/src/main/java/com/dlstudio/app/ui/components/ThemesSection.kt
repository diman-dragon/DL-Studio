package com.dlstudio.app.ui.components

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
import com.dlstudio.app.content.Topic
import com.dlstudio.app.ui.AppState
import com.dlstudio.app.ui.AppViewModel
import com.dlstudio.app.ui.Notice
import com.dlstudio.app.ui.theme.LocalStrings

/** Раздел «Темы» в настройках: добавить тему из файла (json или zip) и список установленных тем (курс → урок → тема) с удалением. */
@Composable
fun ThemesSection(state: AppState, vm: AppViewModel, onPickFile: () -> Unit) {
    val s = LocalStrings.current
    var removing by remember { mutableStateOf<List<Topic>>(emptyList()) }
    var removingAll by remember { mutableStateOf(false) }

    SettingsSection(s.sectionTopics) {
        ActionTile(s.fromFile, onPickFile)
        if (state.notice == Notice.FILE_FAILED) Text(s.fileFailed, Modifier.padding(horizontal = 6.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
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
