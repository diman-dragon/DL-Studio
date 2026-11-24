package com.dlstudio.app.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dlstudio.app.content.Topic
import com.dlstudio.app.content.TopicStore
import com.dlstudio.app.data.Prefs
import com.dlstudio.app.progress.Progress
import com.dlstudio.app.progress.Stats
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
import com.dlstudio.app.speech.Role
import com.dlstudio.app.speech.LangNeed
import com.dlstudio.app.speech.Speaker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Состояние приложения: настройки, темы на устройстве, прогресс, озвучка. Тяжёлое (файлы) — не на главном потоке. */
class AppViewModel(private val app: Application) : AndroidViewModel(app) {
    private val prefs = Prefs(app)
    private val store = TopicStore(app)
    private val speaker = Speaker(app)
    private val listener = Listener(app)
    private val mutable = MutableStateFlow(AppState(prefs.theme, prefs.lang, prefs.accent, ProgressJson.decode(prefs.progressJson), prefs.dailyGoal, lastTopic = prefs.lastTopic, speech = SpeechUi(chosen = prefs.voices, rate = prefs.speechRate)))
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

    /** Папка медиа темы: туда кладутся аудио, видео и картинки урока. */
    fun mediaDir(id: String) = store.mediaDir(id)

    /** Ставит темы из файла (json или zip). После успеха предлагает скачать языковые пакеты, которых темам не хватает. */
    fun installFile(uri: Uri) {
        viewModelScope.launch {
            val (topics, ok) = withContext(Dispatchers.IO) {
                val bytes = runCatching { app.contentResolver.openInputStream(uri)?.use { it.readBytes() } }.getOrNull()
                val installed = bytes?.let(store::install)
                store.installed() to !installed.isNullOrEmpty()
            }
            mutable.update { it.copy(topics = topics, notice = if (ok) Notice.NONE else Notice.FILE_FAILED, offer = ok) }
            if (ok) loadVoices(topics.map { it.tag }.filter { it.isNotBlank() }.distinct())
        }
    }

    fun clearOffer() = mutable.update { it.copy(offer = false) }

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

    /** Запоминает последнюю открытую тему: по ней карта раскрывает урок и встаёт на тему при следующем запуске. */
    fun visit(id: String) {
        if (id == mutable.value.lastTopic) return
        prefs.lastTopic = id
        mutable.update { it.copy(lastTopic = id) }
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
    fun seeCard(topic: Topic, index: Int) { count { it.cardSeen(Xp.dayNumber()) }; reward(Xp.card(mutable.value.progress, topic, index, Xp.dayNumber())) }
    fun answer(topic: Topic, index: Int, correct: Boolean) {
        count { it.answered(topic.quiz[index].kind.name, topic.id, correct, Xp.dayNumber()) }
        reward(Xp.answer(mutable.value.progress, topic, index, correct, Xp.dayNumber()))
    }

    /** Просмотрена страница теории (в статистику; очков не даёт). */
    fun seePage() = count { it.pageSeen() }

    /** Меняет статистику и сразу сохраняет. */
    private fun count(change: (Stats) -> Stats) {
        val progress = mutable.value.progress.let { it.copy(stats = change(it.stats)) }
        prefs.progressJson = ProgressJson.encode(progress)
        mutable.update { it.copy(progress = progress) }
    }
    /** Сброс прогресса: этапы тем и серия обнуляются, XP (и уровень), дневная история XP и статистика сохраняются. Установленные темы и настройки остаются. */
    fun resetProgress() {
        val old = mutable.value.progress
        val kept = Progress(xp = old.xp, history = old.history, stats = old.stats)
        prefs.progressJson = ProgressJson.encode(kept)
        mutable.update { it.copy(progress = kept, gain = null) }
    }

    fun clearGain() = mutable.update { it.copy(gain = null) }

    val speaking = speaker.speaking

    fun speak(text: String, tag: String, role: Role = Role()) { count { it.listened() }; speaker.speak(text, tag, role) }

    private fun updateSpeech(change: (SpeechUi) -> SpeechUi) = mutable.update { it.copy(speech = change(it.speech)) }

    /** Читает голоса встроенного движка и узнаёт, скачаны ли пакеты распознавания для языков `tags`. Вызывать при открытии настроек. */
    fun loadVoices(tags: List<String>) {
        speaker.voices { raw -> updateSpeech { it.copy(voices = raw, loaded = true) } }
        checkPacks(tags)
    }

    private fun checkPacks(tags: List<String>) = tags.forEach { tag ->
        SpeechPacks.check(app, tag) { found -> updateSpeech { it.copy(packs = it.packs + (tag to found)) } }
    }

    /** Выбор голоса: `key` — язык («en», основной голос) или язык с полом («en:f», «en:m», голоса диалогов); `name` — служебное имя, null — автоматически. */
    fun chooseVoice(key: String, name: String?) {
        val chosen = if (name == null) mutable.value.speech.chosen - key else mutable.value.speech.chosen + (key to name)
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

    /**
     * Скачивает всё недостающее сразу: пакеты распознавания (Android 13+ — без экранов, иначе открываются настройки голосового ввода) и голоса —
     * тихо скачать голос Android не позволяет, поэтому открывается экран загрузки голосов самого движка. На старых Android за раз открывается один экран.
     */
    fun downloadNeeded(needs: List<LangNeed>) {
        val packs = needs.flatMap { it.packs }
        val voices = needs.any { it.voices }
        when {
            SpeechPacks.canManage(app) -> { packs.forEach(::downloadPack); if (voices) downloadVoice() }
            packs.isNotEmpty() -> downloadPack(packs.first())
            voices -> downloadVoice()
        }
    }

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
            { heard ->
                val score = Pronunciation.score(text, heard)
                count { it.pronounced(score, Pronunciation.grade(score), Xp.dayNumber()) }
                mutable.update { it.copy(pronounce = Pronounce(text, Phase.DONE, heard.first(), score)) }
            },
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
