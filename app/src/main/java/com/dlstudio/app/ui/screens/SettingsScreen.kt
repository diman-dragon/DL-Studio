package com.dlstudio.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dlstudio.app.ui.AppState
import com.dlstudio.app.ui.AppViewModel
import com.dlstudio.app.ui.components.ScreenHeader
import com.dlstudio.app.ui.components.SettingsData
import com.dlstudio.app.ui.components.SettingsFonts
import com.dlstudio.app.ui.components.SettingsLook
import com.dlstudio.app.ui.components.SettingsSpeech
import com.dlstudio.app.ui.components.VoicePicker
import com.dlstudio.app.ui.theme.LocalStrings

/** Настройки по разделам. Включение напоминаний на Android 13+ сначала запрашивает разрешение на уведомления. */
@Composable
fun SettingsScreen(state: AppState, vm: AppViewModel, onFolder: () -> Unit) {
    val s = LocalStrings.current
    val voices by vm.voices.collectAsStateWithLifecycle()
    var chooseVoice by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), Arrangement.spacedBy(12.dp)) {
        ScreenHeader(s.tabSettings)
        SettingsLook(state, vm)
        SettingsFonts(state, vm)
        SettingsSpeech(state, vm.uiLocale(), vm) { chooseVoice = true; vm.loadVoices() }
        SettingsData(state, onFolder)
    }
    if (chooseVoice) VoicePicker(voices, state.speechVoiceName, vm.uiLocale(), vm::preview, { vm.setSpeechVoice(it); chooseVoice = false }) { chooseVoice = false }
}
