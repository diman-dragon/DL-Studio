package com.dlstudio.app.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dlstudio.app.content.CatalogEntry
import com.dlstudio.app.content.Remote
import com.dlstudio.app.content.Topic
import com.dlstudio.app.content.TopicStore
import com.dlstudio.app.data.Prefs
import com.dlstudio.app.progress.ProgressJson
import com.dlstudio.app.progress.Reward
import com.dlstudio.app.progress.Xp
import com.dlstudio.app.settings.AccentColor
import com.dlstudio.app.settings.AppLang
import com.dlstudio.app.settings.ThemeMode
import com.dlstudio.app.speech.Speaker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Состояние приложения: настройки, темы на устройстве, прогресс и загрузка каталога. Тяжёлое (файлы, сеть) — не на главном потоке. */
class AppViewModel(private val app: Application) : AndroidViewModel(app) {
    private val prefs = Prefs(app)
    private val store = TopicStore(app, prefs)
    private val speaker = Speaker(app)
    private val mutable = MutableStateFlow(AppState(prefs.theme, prefs.lang, prefs.accent, prefs.catalogUrl, ProgressJson.decode(prefs.progressJson)))
    val state: StateFlow<AppState> = mutable.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            store.seed()
            reload()
        }
    }

    private fun reload() {
        val topics = store.installed()
        mutable.update { it.copy(topics = topics) }
    }

    fun setTheme(theme: ThemeMode) { prefs.theme = theme; mutable.update { it.copy(theme = theme) } }
    fun setLang(lang: AppLang) { prefs.lang = lang; mutable.update { it.copy(lang = lang) } }
    fun setAccent(accent: AccentColor) { prefs.accent = accent; mutable.update { it.copy(accent = accent) } }

    fun setCatalogUrl(url: String) {
        prefs.catalogUrl = url
        mutable.update { it.copy(catalogUrl = prefs.catalogUrl, catalog = emptyList(), catalogFailed = false) }
    }

    fun refreshCatalog() {
        val url = mutable.value.catalogUrl
        if (url.isBlank() || mutable.value.catalogLoading) return
        mutable.update { it.copy(catalogLoading = true, catalogFailed = false) }
        viewModelScope.launch(Dispatchers.IO) {
            val result = runCatching { Remote.catalog(url) }
            mutable.update { it.copy(catalogLoading = false, catalog = result.getOrDefault(it.catalog), catalogFailed = result.isFailure) }
        }
    }

    /** Скачивает выбранные темы по одной; ошибка одной не мешает остальным. */
    fun download(entries: List<CatalogEntry>) {
        val url = mutable.value.catalogUrl
        if (entries.isEmpty() || mutable.value.busy.isNotEmpty()) return
        mutable.update { it.copy(busy = entries.map(CatalogEntry::id).toSet(), notice = Notice.NONE) }
        viewModelScope.launch(Dispatchers.IO) {
            var failed = false
            entries.forEach { entry ->
                if (runCatching { store.install(Remote.download(url, entry)) }.getOrNull() == null) failed = true
                mutable.update { it.copy(busy = it.busy - entry.id) }
            }
            val topics = store.installed()
            mutable.update { it.copy(topics = topics, busy = emptySet(), notice = if (failed) Notice.DOWNLOAD_FAILED else Notice.NONE) }
        }
    }

    fun installFile(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val bytes = runCatching { app.contentResolver.openInputStream(uri)?.use { it.readBytes() } }.getOrNull()
            val installed = bytes?.let(store::install)
            val topics = store.installed()
            mutable.update { it.copy(topics = topics, notice = if (installed == null) Notice.FILE_FAILED else Notice.NONE) }
        }
    }

    fun remove(topic: Topic) {
        viewModelScope.launch(Dispatchers.IO) {
            store.remove(topic.id)
            reload()
        }
    }

    private fun reward(r: Reward) {
        if (r.gained == 0) return
        prefs.progressJson = ProgressJson.encode(r.progress)
        mutable.update { it.copy(progress = r.progress, gain = Gain(r.gained, r.finished, (it.gain?.id ?: 0) + 1)) }
    }

    fun finishTheory(topic: Topic) = reward(Xp.theory(mutable.value.progress, topic, Xp.dayNumber()))
    fun knowCard(topic: Topic, index: Int) = reward(Xp.card(mutable.value.progress, topic, index, Xp.dayNumber()))
    fun answer(topic: Topic, index: Int, correct: Boolean) = reward(Xp.answer(mutable.value.progress, topic, index, correct, Xp.dayNumber()))
    fun clearGain() = mutable.update { it.copy(gain = null) }

    fun speak(text: String, tag: String) = speaker.speak(text, tag)

    override fun onCleared() { speaker.shutdown() }
}
