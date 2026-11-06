package com.dlstudio.app.ui.components

import androidx.compose.runtime.Composable
import com.dlstudio.app.settings.AppLang
import com.dlstudio.app.settings.ThemeMode
import com.dlstudio.app.ui.AppState
import com.dlstudio.app.ui.AppViewModel
import com.dlstudio.app.ui.theme.LocalStrings

/** Раздел «Внешний вид»: тема, язык, акцентный цвет. */
@Composable
fun SettingsLook(state: AppState, vm: AppViewModel) {
    val s = LocalStrings.current
    SettingsSection(s.sectionLook) {
        OptionChips(s.settingTheme, ThemeMode.values().map { s.themeName(it) to it }, state.theme, vm::setTheme)
        OptionChips(s.settingLanguage, AppLang.values().map { it.label to it }, state.lang, vm::setLang)
        AccentTile(s.settingAccent, state.accent, s::accentName, vm::setAccent)
    }
}
