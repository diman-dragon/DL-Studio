package com.dlstudio.app.ui.components

import androidx.compose.runtime.Composable
import com.dlstudio.app.settings.StorageStatus
import com.dlstudio.app.storage.StorageLayout
import com.dlstudio.app.ui.AppState
import com.dlstudio.app.ui.theme.LocalStrings

/** Раздел «Данные»: папка хранения с состоянием переноса и «О нас». */
@Composable
fun SettingsData(state: AppState, onFolder: () -> Unit) {
    val s = LocalStrings.current
    val moving = state.status == StorageStatus.MOVING
    SettingsSection(s.sectionData) {
        SettingTile(s.settingFolder, state.folderName?.let { "$it/${StorageLayout.ROOT}" } ?: s.folderDefault, onFolder, enabled = !moving)
        StatusText(s.status(state.status), moving)
        AboutCard()
    }
}
