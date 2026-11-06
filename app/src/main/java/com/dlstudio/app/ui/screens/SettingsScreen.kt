package com.dlstudio.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import com.dlstudio.app.BuildConfig
import com.dlstudio.app.settings.AccentColor
import com.dlstudio.app.settings.StorageStatus
import com.dlstudio.app.storage.StorageLayout
import com.dlstudio.app.ui.AppState
import com.dlstudio.app.ui.components.AboutDialog
import com.dlstudio.app.ui.components.AccentTile
import com.dlstudio.app.ui.components.ScreenHeader
import com.dlstudio.app.ui.components.SettingTile
import com.dlstudio.app.ui.components.StatusText
import com.dlstudio.app.ui.components.VoicePicker
import com.dlstudio.app.ui.theme.LocalStrings

/** Настройки: переключатели по тапу по кругу (тема, язык), выбор акцентного цвета, папка, «О нас». */
@Composable
fun SettingsScreen(state: AppState, onTheme: () -> Unit, onLang: () -> Unit, onAccent: (AccentColor) -> Unit, onFolder: () -> Unit, onSpeechRate: () -> Unit, onSpeechPitch: () -> Unit, onSpeechVoice: (String?) -> Unit, onDailyCards: () -> Unit, onReminders: () -> Unit, onReminderTime: () -> Unit) {
    val s = LocalStrings.current
    var about by remember { mutableStateOf(false) }; var chooseVoice by remember { mutableStateOf(false) }; var voices by remember { mutableStateOf(emptyList<Voice>()) }; var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    val context = LocalContext.current
    DisposableEffect(Unit) { lateinit var e: TextToSpeech; e = TextToSpeech(context) { if (it == TextToSpeech.SUCCESS) voices = e.voices?.filter { !it.isNetworkConnectionRequired } ?: emptyList() }; tts = e; onDispose { e.shutdown() } }
    val moving = state.status == StorageStatus.MOVING
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ScreenHeader(s.tabSettings)
        SettingTile(s.settingTheme, s.themeName(state.theme), onTheme)
        SettingTile(s.settingLanguage, state.lang.label, onLang)
        AccentTile(s.settingAccent, state.accent, s::accentName, onAccent)
        SettingTile(s.settingFolder, state.folderName?.let { "$it/${StorageLayout.ROOT}" } ?: s.folderDefault, onFolder, enabled = !moving)
        SettingTile(s.settingAbout, BuildConfig.VERSION_NAME, { about = true })
        SettingTile(s.speechVoice, state.speechVoiceName?.replace('_', ' ') ?: state.lang.label, { chooseVoice = true })
        SettingTile(s.speechRate, "${state.speechRate}×", onSpeechRate)
        SettingTile(s.speechPitch, "${state.speechPitch}×", onSpeechPitch)
        SettingTile(s.dailyCards, "${state.dailyCards} за одну сессию", onDailyCards)
        SettingTile(s.reminders, if (state.remindersEnabled) "Включены" else "Выключены", onReminders)
        if (state.remindersEnabled) SettingTile(s.reminderTime, "%02d:%02d".format(state.reminderHour, state.reminderMinute), onReminderTime)
        StatusText(s.status(state.status), moving)
    }
    if (about) AboutDialog { about = false }
    if (chooseVoice) VoicePicker(voices, state.speechVoiceName, tts, { onSpeechVoice(it.name); chooseVoice = false }, { chooseVoice = false })
}
