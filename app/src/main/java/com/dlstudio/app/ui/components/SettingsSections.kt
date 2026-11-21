package com.dlstudio.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dlstudio.app.settings.AppLang
import com.dlstudio.app.settings.FontGroup
import com.dlstudio.app.settings.FontPreset
import com.dlstudio.app.settings.FontSettings
import com.dlstudio.app.settings.StorageStatus
import com.dlstudio.app.settings.ThemeMode
import com.dlstudio.app.storage.StorageLayout
import com.dlstudio.app.ui.AppState
import com.dlstudio.app.ui.AppViewModel
import com.dlstudio.app.ui.theme.LocalStrings
import java.util.Locale

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title.uppercase(), Modifier.padding(start = 6.dp, top = 8.dp), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        content()
    }
}

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

@Composable
fun SettingsLook(state: AppState, vm: AppViewModel) {
    val s = LocalStrings.current
    SettingsSection(s.sectionLook) {
        SettingRow {
            CycleTile(s.settingTheme, ThemeMode.values().map { s.themeName(it) to it }, state.theme, vm::setTheme, Modifier.weight(1f))
            CycleTile(s.settingLanguage, AppLang.values().map { it.label to it }, state.lang, vm::setLang, Modifier.weight(1f))
        }
        AccentTile(s.settingAccent, state.accent, s::accentName, vm::setAccent)
    }
}

@Composable
fun SettingsSpeech(state: AppState, ui: Locale, vm: AppViewModel, onVoice: () -> Unit) {
    val s = LocalStrings.current
    SettingsSection(s.sectionSpeech) {
        SettingTile(s.speechVoice, state.speechVoiceLabel ?: state.speechVoiceName?.let { VoiceName.fallback(it, ui) } ?: state.lang.label, onVoice)
    }
}

@Composable
fun SettingsFonts(state: AppState, vm: AppViewModel) {
    val s = LocalStrings.current
    val fonts = state.fonts
    SettingsSection(s.sectionFonts) {
        OptionChips(s.fontPresetTitle, FontPreset.values().map { s.fontPresetName(it) to it }, fonts.preset, vm::setFontPreset)
        Text(s.fontPresetHint(fonts.preset), Modifier.padding(horizontal = 6.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (fonts.preset == FontPreset.CUSTOM) FontGroup.values().forEach { group -> FontTile(group, fonts[group]) { vm.setFont(group, it) } }
        if (fonts != FontSettings()) SettingTile(s.fontReset, s.fontResetHint, vm::resetFonts)
    }
}
