package com.dlstudio.app.ui.components

import androidx.compose.runtime.Composable
import com.dlstudio.app.ui.AppState
import com.dlstudio.app.ui.AppViewModel
import com.dlstudio.app.ui.theme.LocalStrings
import kotlin.math.abs

/** Раздел «Озвучка»: голос (список), скорость и высота. */
@Composable
fun SettingsSpeech(state: AppState, vm: AppViewModel, onVoice: () -> Unit) {
    val s = LocalStrings.current
    val rates = listOf(0.8f, 1f, 1.2f)
    val pitches = listOf(0.9f, 1f, 1.1f)
    SettingsSection(s.sectionSpeech) {
        SettingTile(s.speechVoice, state.speechVoiceName?.replace('_', ' ') ?: state.lang.label, onVoice)
        OptionChips(s.speechRate, rates.map { "$it×" to it }, rates.minByOrNull { abs(it - state.speechRate) } ?: 1f, vm::setSpeechRate)
        OptionChips(s.speechPitch, pitches.map { "$it×" to it }, pitches.minByOrNull { abs(it - state.speechPitch) } ?: 1f, vm::setSpeechPitch)
    }
}
