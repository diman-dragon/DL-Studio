package com.dlstudio.app.speech

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

private const val SAFETY_MS = 3000L
private const val SAFETY_PER_CHAR_MS = 120L
private const val SILENT_MS = 700L
private const val SILENT_PER_CHAR_MS = 55L
private const val GAP_MS = 350L

/** Кто сейчас говорит: ID персонажа (null — пробное воспроизведение голоса), произносимый текст и метка строки (какая именно реплика на экране). По ним аватары двигают ртом. */
data class Speaking(val characterId: String?, val text: String, val line: String? = null)

/** Одна реплика: текст, ID персонажа (null — мисс Грейс) и метка строки на экране. */
data class Utter(val text: String, val who: String?, val line: String? = null)

/** Озвучить реплику голосом персонажа (ID из Characters; null — мисс Грейс); третий аргумент — метка строки, чтобы двигал ртом именно её аватар. */
typealias SpeakAs = (String, String?, String?) -> Unit

/** Озвучить текст голосом мисс Грейс. */
typealias Speak = (String) -> Unit

/** Озвучить диалог: реплики подряд, каждая своим голосом. */
typealias SpeakAll = (List<Utter>) -> Unit

/** Почему озвучки нет: у системы нет движка синтеза речи или для языка темы нет голоса. Текст при этом остаётся на экране, аватары «говорят» без звука. */
enum class SpeechProblem { NO_ENGINE, NO_LANGUAGE }

/**
 * Один встроенный движок синтеза речи Android на приложение: создаётся при первой озвучке, а не при каждом входе на экран.
 * Голоса персонажей распределяются сразу для всех (см. VoiceCast): `chosen` — выбранные в настройках («en:grace»…), `sticky` — подобранные и закреплённые
 * (при появлении нового голоса в системе персонаж остаётся при своём; изменения приходят в `onSticky`), `rate` — скорость речи.
 * `speaking` — кто и что говорит сейчас (null — тишина); `problem` — почему озвучки нет. Если звука нет, реплика всё равно «звучит» по времени, и аватар двигается.
 */
class Speaker(private val context: Context) {
    private var tts: TextToSpeech? = null
    private var ready = false
    private var failed = false
    private val queued = ArrayList<() -> Unit>()
    private val defaults = HashMap<String, String?>()
    private val lines = ArrayDeque<Utter>()
    private var linesTag = ""
    var chosen: Map<String, String> = emptyMap()
    var sticky: Map<String, String> = emptyMap()
    var onSticky: (Map<String, String>) -> Unit = {}
    var rate: Float = 1f
    private val talk = MutableStateFlow<Speaking?>(null)
    val speaking: StateFlow<Speaking?> = talk
    private val trouble = MutableStateFlow<SpeechProblem?>(null)
    val problem: StateFlow<SpeechProblem?> = trouble
    private var current = ""
    private var finished = ""
    private var counter = 0
    private val handler = Handler(Looper.getMainLooper())
    private val listener = object : UtteranceProgressListener() {
        override fun onStart(utteranceId: String?) {}
        override fun onDone(utteranceId: String?) = end(utteranceId)
        @Deprecated("Deprecated in Java")
        override fun onError(utteranceId: String?) = end(utteranceId)
        override fun onStop(utteranceId: String?, interrupted: Boolean) = end(utteranceId)
    }

    /** Окончание реплики приходит из потока движка; всё остальное делается в главном потоке. */
    private fun end(id: String?) { if (id != null) handler.post { complete(id) } }

    /** Реплика закончилась (или сработал таймер): снять отметку и, если идёт диалог, начать следующую реплику. Повторные сигналы одной реплики игнорируются. */
    private fun complete(id: String) {
        if (id != current || id == finished) return
        finished = id
        talk.value = null
        val next = lines.removeFirstOrNull() ?: return
        handler.postDelayed({ if (id == current) play(next, linesTag) }, GAP_MS)
    }

    /** Отмечает, кто говорит, и ставит таймер окончания (на случай, если движок не сообщит об окончании или звука нет вовсе). */
    private fun begin(u: Utter, profile: CharacterProfile, millis: Long): String {
        current = "dl${++counter}"
        val id = current
        talk.value = Speaking(profile.id, u.text, u.line)
        handler.postDelayed({ complete(id) }, millis)
        return id
    }

    private fun say(e: TextToSpeech, u: Utter, profile: CharacterProfile) {
        val id = begin(u, profile, SAFETY_MS + u.text.length * SAFETY_PER_CHAR_MS)
        e.setOnUtteranceProgressListener(listener)
        e.speak(u.text, TextToSpeech.QUEUE_FLUSH, null, id)
    }

    /** Звука нет (нет движка или голоса языка): реплика «звучит» по времени, чтобы аватар и подсветка строки шли в такт тексту. */
    private fun silent(u: Utter, profile: CharacterProfile) { begin(u, profile, SILENT_MS + u.text.length * SILENT_PER_CHAR_MS) }

    private fun ensureEngine(): TextToSpeech = tts ?: TextToSpeech(context.applicationContext) { code ->
        ready = code == TextToSpeech.SUCCESS
        failed = !ready
        if (failed) trouble.value = SpeechProblem.NO_ENGINE
        queued.forEach { it() }
        queued.clear()
    }.also { tts = it }

    private fun whenReady(job: () -> Unit) {
        ensureEngine()
        if (ready || failed) job() else queued.add(job)
    }

    /** Озвучивает реплику голосом персонажа `who` (null — мисс Грейс): выбранный в настройках, закреплённый или лучший свободный (см. VoiceCast). Прерывает диалог. */
    fun speak(text: String, tag: String, who: String? = null, line: String? = null) {
        lines.clear()
        play(Utter(text, who, line), tag)
    }

    /** Озвучивает диалог: реплики подряд, каждая голосом своего персонажа; аватар каждой оживает вовремя. Новая озвучка или `stop` прерывают диалог. */
    fun speakAll(items: List<Utter>, tag: String) {
        if (items.isEmpty()) return
        lines.clear()
        lines.addAll(items.drop(1))
        linesTag = tag
        play(items.first(), tag)
    }

    private fun play(u: Utter, tag: String) = whenReady {
        val profile = Characters.of(u.who)
        val e = tts?.takeIf { ready }
        if (e == null) return@whenReady silent(u, profile)
        val locale = Locale.forLanguageTag(tag)
        if (prepare(e, locale) < 0) {
            trouble.value = SpeechProblem.NO_LANGUAGE
            return@whenReady silent(u, profile)
        }
        trouble.value = null
        val cast = castFor(e, tag)[profile.id] ?: Cast(null)
        applyVoice(e, cast.voice ?: defaults[locale.language], locale)
        e.setPitch(cast.pitch)
        e.setSpeechRate(rate * cast.rate)
        say(e, u, profile)
    }

    /** Распределение голосов для языка темы; новые собственные голоса закрепляются. */
    private fun castFor(e: TextToSpeech, tag: String): Map<String, Cast> {
        val lang = VoiceNames.language(tag)
        val plan = VoiceCast.plan(rawVoices(e), tag, chosen, sticky)
        val fresh = plan.filter { (id, cast) -> cast.how == How.OWN && cast.voice != null && sticky["$lang:$id"] != cast.voice }
        if (fresh.isNotEmpty()) {
            sticky = sticky + fresh.map { (id, cast) -> "$lang:$id" to cast.voice!! }
            onSticky(sticky)
        }
        return plan
    }

    /** Язык ставится всегда (результат — код движка, отрицательный: нет данных или язык не поддерживается); голос по умолчанию запоминается до первой смены голоса, чтобы к нему можно было вернуться. */
    private fun prepare(e: TextToSpeech, locale: Locale): Int {
        val code = e.setLanguage(locale)
        if (!defaults.containsKey(locale.language)) defaults[locale.language] = runCatching { e.voice?.name }.getOrNull()
        return code
    }

    private fun rawVoices(e: TextToSpeech): List<RawVoice> = runCatching { e.voices }.getOrNull().orEmpty().map {
        RawVoice(it.name, it.locale.toLanguageTag(), it.quality, it.isNetworkConnectionRequired, TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED !in it.features.orEmpty(), Gender.of(it.name, it.features.orEmpty()))
    }

    /** Пробное воспроизведение конкретного голоса из списка настроек (выбор при этом не меняется). */
    fun preview(text: String, voice: String, tag: String) = whenReady {
        val e = tts?.takeIf { ready } ?: return@whenReady
        lines.clear()
        prepare(e, Locale.forLanguageTag(tag))
        applyVoice(e, voice, Locale.forLanguageTag(tag))
        e.setPitch(1f)
        e.setSpeechRate(rate)
        val id = begin(Utter(text, null), Characters.of(null), SAFETY_MS + text.length * SAFETY_PER_CHAR_MS)
        talk.value = Speaking(null, text)
        e.setOnUtteranceProgressListener(listener)
        e.speak(text, TextToSpeech.QUEUE_FLUSH, null, id)
    }

    /** Ставит голос, если он есть у движка и подходит по языку; иначе остаётся голос по умолчанию для языка. */
    private fun applyVoice(e: TextToSpeech, name: String?, locale: Locale) {
        if (name == null) return
        val voice = runCatching { e.voices }.getOrNull()?.firstOrNull { it.name == name && it.locale.language == locale.language } ?: return
        e.setVoice(voice)
    }

    /** Все голоса движка (в том числе ещё не скачанные); список придёт, когда движок будет готов (если движка нет — пустой). */
    fun voices(onReady: (List<RawVoice>) -> Unit) = whenReady { onReady(tts?.takeIf { ready }?.let(::rawVoices).orEmpty()) }

    /** Пакет движка синтеза речи по умолчанию (например com.google.android.tts); null, если движок ещё не запущен. */
    fun engine(): String? = runCatching { tts?.defaultEngine }.getOrNull()

    /** Прервать озвучку и диалог (перед записью микрофона, чтобы динамик не попал в запись). */
    fun stop() { lines.clear(); current = ""; tts?.stop(); talk.value = null }

    fun shutdown() {
        handler.removeCallbacksAndMessages(null); lines.clear(); tts?.stop(); tts?.shutdown()
        tts = null; ready = false; failed = false; queued.clear(); defaults.clear(); talk.value = null
    }
}
