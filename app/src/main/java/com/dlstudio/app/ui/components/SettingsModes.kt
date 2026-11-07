package com.dlstudio.app.ui.components

import androidx.compose.runtime.Composable
import com.dlstudio.app.study.CardMode
import com.dlstudio.app.ui.AppState
import com.dlstudio.app.ui.AppViewModel
import com.dlstudio.app.ui.theme.LocalStrings

/** Разделы «Режимы карточек» (какие упражнения показывать; хотя бы один включён) и «Помощники» (микрофон, поздравления). */
@Composable
fun SettingsModes(state: AppState, vm: AppViewModel) {
    val s = LocalStrings.current
    val study = state.study
    fun label(on: Boolean) = if (on) s.on else s.off
    SettingsSection(s.sectionModes) {
        CardMode.values().forEach { mode ->
            val on = mode in study.modes
            val last = study.isLastMode(mode)
            SwitchTile(s.modeName(mode), if (last) s.modeLast else label(on), on, { vm.setStudy(study.withMode(mode, it)) }, enabled = !last)
        }
    }
    SettingsSection(s.sectionHelpers) {
        SwitchTile(s.pronounceSetting, label(study.pronounce), study.pronounce, { vm.setStudy(study.copy(pronounce = it)) })
        SwitchTile(s.celebrateSetting, label(study.celebrate), study.celebrate, { vm.setStudy(study.copy(celebrate = it)) })
    }
}
