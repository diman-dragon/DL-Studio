package com.dlstudio.app.ui

import android.app.Application
import android.net.Uri
import android.speech.tts.Voice
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dlstudio.app.data.Prefs
import com.dlstudio.app.data.folderName
import com.dlstudio.app.data.moveToFolder
import com.dlstudio.app.data.openParent
import com.dlstudio.app.settings.AccentColor
import com.dlstudio.app.settings.AppLang
import com.dlstudio.app.settings.StorageStatus
import com.dlstudio.app.settings.ThemeMode
import com.dlstudio.app.study.Speaker
import com.dlstudio.app.study.StudyReminderScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.dlstudio.app.storage.ensureLayout

/** Настройки приложения и озвучка. Каждая настройка пишется в `Prefs` и сразу отражается в состоянии. */
class AppViewModel(private val app: Application) : AndroidViewModel(app) {
    private val prefs = Prefs(app)
    private val speaker = Speaker(app)
    private val mutable = MutableStateFlow(
        AppState(prefs.theme, prefs.lang, prefs.accent, folderName(app, prefs), prefs.speechRate, prefs.speechPitch, prefs.speechVoiceName,
            sessionSize = prefs.sessionSize, remindersEnabled = prefs.remindersEnabled, reminderHour = prefs.reminderHour)
    )
    val state: StateFlow<AppState> = mutable.asStateFlow()
    private val voiceList = MutableStateFlow<List<Voice>>(emptyList())
    val voices: StateFlow<List<Voice>> = voiceList.asStateFlow()

    init { StudyReminderScheduler.sync(app, prefs); viewModelScope.launch(Dispatchers.IO) { runCatching { ensureLayout(openParent(app, prefs)) } } }

    fun setTheme(theme: ThemeMode) { prefs.theme = theme; mutable.update { it.copy(theme = theme) } }
    fun setLang(lang: AppLang) { prefs.lang = lang; mutable.update { it.copy(lang = lang) } }
    fun setAccent(accent: AccentColor) { prefs.accent = accent; mutable.update { it.copy(accent = accent) } }
    fun setSessionSize(size: Int) { prefs.sessionSize = size; mutable.update { it.copy(sessionSize = size) } }
    fun setSpeechRate(rate: Float) { prefs.speechRate = rate; mutable.update { it.copy(speechRate = rate) } }
    fun setSpeechPitch(pitch: Float) { prefs.speechPitch = pitch; mutable.update { it.copy(speechPitch = pitch) } }
    fun setSpeechVoice(name: String?) { prefs.speechVoiceName = name; mutable.update { it.copy(speechVoiceName = name) } }
    fun setReminders(enabled: Boolean) { prefs.remindersEnabled = enabled; StudyReminderScheduler.sync(app, prefs); mutable.update { it.copy(remindersEnabled = enabled) } }
    fun setReminderHour(hour: Int) { prefs.reminderHour = hour; prefs.reminderMinute = 0; StudyReminderScheduler.sync(app, prefs); mutable.update { it.copy(reminderHour = hour) } }

    fun speak(text: String) { if (text.isNotBlank()) mutable.value.let { speaker.speak(text, it.speechRate, it.speechPitch, it.speechVoiceName) } }
    fun loadVoices() { viewModelScope.launch(Dispatchers.Default) { voiceList.value = speaker.voices() } }
    fun preview(voice: Voice, text: String) = mutable.value.let { speaker.preview(voice, text, it.speechRate, it.speechPitch) }

    /** Перенос папки хранения: база закрывается до переноса (`before`) и открывается на новом месте (`after`). */
    fun pickFolder(tree: Uri, before: suspend () -> Unit, after: suspend () -> Unit) {
        if (mutable.value.status == StorageStatus.MOVING) return
        mutable.update { it.copy(status = StorageStatus.MOVING) }
        viewModelScope.launch {
            before()
            val status = moveToFolder(app, prefs, tree)
            after()
            mutable.update { it.copy(folderName = folderName(app, prefs), status = status) }
        }
    }

    override fun onCleared() { speaker.shutdown(); super.onCleared() }
}
