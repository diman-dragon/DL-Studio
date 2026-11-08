package com.dlstudio.app.ui.components

import androidx.compose.runtime.Composable
import com.dlstudio.app.ui.AppState
import com.dlstudio.app.ui.theme.LocalStrings

/** Раздел «Озвучка»: выбранный голос под его человеческим именем (так же, как в списке). Скорость — кнопками «медленно» на карточках. */
@Composable
fun SettingsSpeech(state: AppState, onVoice: () -> Unit) {
    val s = LocalStrings.current
    val name = state.speechVoiceName
    SettingsSection(s.sectionSpeech) {
        SettingTile(s.speechVoice, state.speechVoiceLabel ?: name?.let(VoiceName::fallback) ?: state.lang.label, onVoice)
    }
}
