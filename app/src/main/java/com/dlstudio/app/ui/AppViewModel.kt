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
import com.dlstudio.app.progress.Progress
import com.dlstudio.app.progress.ProgressJson
import com.dlstudio.app.progress.Reward
import com.dlstudio.app.progress.Week
import com.dlstudio.app.progress.Xp
import com.dlstudio.app.settings.AccentColor
import com.dlstudio.app.settings.AppLang
import com.dlstudio.app.settings.ThemeMode
import com.dlstudio.app.speech.Listener
import com.dlstudio.app.speech.PackState
import com.dlstudio.app.speech.SpeechPacks
import com.dlstudio.app.speech.VoiceChoice
import com.dlstudio.app.speech.Phase
import com.dlstudio.app.speech.Pronounce
import com.dlstudio.app.speech.Pronunciation
import com.dlstudio.app.speech.Speaker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Состояние приложения: настройки, темы на устройстве, прогресс и загрузка каталога. Тяжёлое (файлы, сеть) — не на главном потоке. */
class AppViewModel(private val app: Application) : AndroidViewModel(app) {
    private val prefs = Prefs(app)
    private val store = TopicStore(app)
    private val speaker = Speaker(app)
    private val listener = Listener(app)
    private val mutable = MutableStateFlow(AppState(prefs.theme, prefs.lang, prefs.accent, prefs.catalogUrl, ProgressJson.decode(prefs.progressJson), prefs.dailyGoal, speech = SpeechUi(chosen = prefs.voices, rate = prefs.speechRate)))
    val state: StateFlow<AppState> = mutable.asStateFlow()

    init {
        speaker.chosen = prefs.voices
        speaker.rate = prefs.speechRate
        viewModelScope.launch(Dispatchers.IO) { reload() }
    }

    private fun reload() {
        val topics = store.installed()
        mutable.update { it.copy(topics = topics) }
    }

    fun setTheme(theme: ThemeMode) { prefs.theme = theme; mutable.update { it.copy(theme = theme) } }
    fun setLang(lang: AppLang) { prefs.lang = lang; mutable.update { it.copy(lang = lang) } }
    fun setAccent(accent: AccentColor) { prefs.accent = accent; mutable.update { it.copy(accent = accent) } }

    fun setDailyGoal(goal: Int) { prefs.dailyGoal = goal; mutable.update { it.copy(dailyGoal = goal) } }

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
                val ok = runCatching { store.install(Remote.download(url, entry)).isNotEmpty() }.getOrDefault(false)
                if (!ok || runCatching { Remote.downloadMedia(url, entry, store.mediaDir(entry.id)) }.isFailure) failed = true
                mutable.update { it.copy(busy = it.busy - entry.id) }
            }
            val topics = store.installed()
            mutable.update { it.copy(topics = topics, busy = emptySet(), notice = if (failed) Notice.DOWNLOAD_FAILED else Notice.NONE) }
        }
    }

    /** Папка медиа темы: туда кладутся аудио, видео и картинки урока. */
    fun mediaDir(id: String) = store.mediaDir(id)

    fun installFile(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val bytes = runCatching { app.contentResolver.openInputStream(uri)?.use { it.readBytes() } }.getOrNull()
            val installed = bytes?.let(store::install)
            val topics = store.installed()
            mutable.update { it.copy(topics = topics, notice = if (installed.isNullOrEmpty()) Notice.FILE_FAILED else Notice.NONE) }
        }
    }

    fun remove(topic: Topic) {
        viewModelScope.launch(Dispatchers.IO) {
            store.remove(topic.id)
            reload()
        }
    }

    /** Удаляет несколько тем (курс или урок целиком); прогресс сохраняется. */
    fun removeMany(topics: List<Topic>) {
        viewModelScope.launch(Dispatchers.IO) {
            topics.forEach { store.remove(it.id) }
            reload()
        }
    }

    fun removeAll() {
        viewModelScope.launch(Dispatchers.IO) {
            store.removeAll()
            reload()
        }
    }

    private fun reward(r: Reward) {
        if (r.gained == 0) return
        prefs.progressJson = ProgressJson.encode(r.progress)
        val today = Xp.dayNumber()
        mutable.update {
            val goal = Week.crossedGoal(it.progress, r.progress, today, it.dailyGoal)
            it.copy(progress = r.progress, gain = Gain(r.gained, r.finished, (it.gain?.id ?: 0) + 1, goal))
        }
    }

    fun finishTheory(topic: Topic) = reward(Xp.theory(mutable.value.progress, topic, Xp.dayNumber()))
    fun seeCard(topic: Topic, index: Int) = reward(Xp.card(mutable.value.progress, topic, index, Xp.dayNumber()))
    fun answer(topic: Topic, index: Int, correct: Boolean) = reward(Xp.answer(mutable.value.progress, topic, index, correct, Xp.dayNumber()))
    /** Сброс прогресса: этапы тем и серия обнуляются, XP (и уровень) и дневная история XP сохраняются. Установленные темы и настройки остаются. */
    fun resetProgress() {
        val old = mutable.value.progress
        val kept = Progress(xp = old.xp, history = old.history)
        prefs.progressJson = ProgressJson.encode(kept)
        mutable.update { it.copy(progress = kept, gain = null) }
    }

    fun clearGain() = mutable.update { it.copy(gain = null) }

    fun speak(text: String, tag: String) = speaker.speak(text, tag)

    private fun updateSpeech(change: (SpeechUi) -> SpeechUi) = mutable.update { it.copy(speech = change(it.speech)) }

    /** Читает голоса встроенного движка и узнаёт, скачаны ли пакеты распознавания для языков `tags`. Вызывать при открытии настроек. */
    fun loadVoices(tags: List<String>) {
        speaker.voices { raw -> updateSpeech { it.copy(voices = raw, loaded = true) } }
        checkPacks(tags)
    }

    private fun checkPacks(tags: List<String>) = tags.forEach { tag ->
        SpeechPacks.check(app, tag) { found -> updateSpeech { it.copy(packs = it.packs + (tag to found)) } }
    }

    /** Выбор голоса для языка (`name` — служебное имя; null — голос по умолчанию). */
    fun chooseVoice(lang: String, name: String?) {
        val chosen = if (name == null) mutable.value.speech.chosen - lang else mutable.value.speech.chosen + (lang to name)
        prefs.voices = chosen
        speaker.chosen = chosen
        updateSpeech { it.copy(chosen = chosen) }
    }

    fun previewVoice(voice: VoiceChoice, sample: String) = speaker.preview(sample, voice.name, voice.tag)

    fun setSpeechRate(rate: Float) {
        prefs.speechRate = rate
        speaker.rate = rate
        updateSpeech { it.copy(rate = rate) }
    }

    /** Открывает экран загрузки голосов самого движка синтеза речи (не общие настройки системы). */
    fun downloadVoice() { SpeechPacks.openVoiceDownload(app, speaker.engine()) }

    /** Скачивание пакета распознавания речи: на Android 13+ запускается сразу и через несколько секунд состояние проверяется ещё раз, на старых открываются настройки голосового ввода. */
    fun downloadPack(tag: String) {
        SpeechPacks.download(app, tag)
        if (!SpeechPacks.canManage(app)) return
        updateSpeech { it.copy(packs = it.packs + (tag to PackState.DOWNLOADING)) }
        viewModelScope.launch { delay(PACK_RECHECK_MS); checkPacks(listOf(tag)) }
    }

    /** Слушает, как пользователь произносит `text`, и оценивает похожесть (см. Pronunciation). */
    fun listen(text: String, tag: String) {
        speaker.stop()
        mutable.update { it.copy(pronounce = Pronounce(text, Phase.LISTENING)) }
        listener.start(
            tag,
            { heard -> mutable.update { it.copy(pronounce = Pronounce(text, Phase.DONE, heard.first(), Pronunciation.score(text, heard))) } },
            { phase -> mutable.update { it.copy(pronounce = Pronounce(text, phase)) } }
        )
    }

    fun stopListening() {
        listener.stop()
        if (mutable.value.pronounce.phase == Phase.LISTENING) mutable.update { it.copy(pronounce = Pronounce()) }
    }

    fun micDenied(text: String) = mutable.update { it.copy(pronounce = Pronounce(text, Phase.NO_PERMISSION)) }

    private companion object { const val PACK_RECHECK_MS = 5000L }

    override fun onCleared() { speaker.shutdown(); listener.stop() }
}
