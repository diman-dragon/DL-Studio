package com.dlstudio.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dlstudio.app.BuildConfig
import com.dlstudio.app.settings.StorageStatus
import com.dlstudio.app.storage.StorageLayout
import com.dlstudio.app.ui.AppState
import com.dlstudio.app.ui.components.AboutDialog
import com.dlstudio.app.ui.components.ScreenHeader
import com.dlstudio.app.ui.components.SettingTile
import com.dlstudio.app.ui.components.StatusText
import com.dlstudio.app.ui.theme.LocalStrings

/** Настройки: переключатели по тапу по кругу (тема, язык), выбор папки, «О нас». */
@Composable
fun SettingsScreen(state: AppState, onTheme: () -> Unit, onLang: () -> Unit, onFolder: () -> Unit) {
    val s = LocalStrings.current
    var about by remember { mutableStateOf(false) }
    val moving = state.status == StorageStatus.MOVING
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ScreenHeader(s.tabSettings)
        SettingTile(s.settingTheme, s.themeName(state.theme), onTheme)
        SettingTile(s.settingLanguage, state.lang.label, onLang)
        SettingTile(s.settingFolder, state.folderName?.let { "$it/${StorageLayout.ROOT}" } ?: s.folderDefault, onFolder, enabled = !moving)
        SettingTile(s.settingAbout, BuildConfig.VERSION_NAME, { about = true })
        StatusText(s.status(state.status), moving)
    }
    if (about) AboutDialog { about = false }
}
