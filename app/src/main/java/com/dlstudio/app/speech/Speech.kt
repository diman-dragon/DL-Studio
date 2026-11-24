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

/** Озвучить текст языком темы (BCP 47, например en-US). */
typealias Speak = (String) -> Unit

/** Кто сейчас говорит: ID персонажа (null — пробное воспроизведение голоса) и произносимый текст. По нему аватары двигают ртом. */
data class Speaking(val characterId: String?, val text: String)

/** Озвучка голосом персонажа (ID из Characters); null — мисс Грейс. */
typealias SpeakAs = (String, String?) -> Unit

/**
 * Один встроенный движок синтеза речи Android на приложение: создаётся при первой озвучке, а не при каждом входе на экран.
 * `chosen` — выбранные голоса персонажей: «en:grace», «en:leo», «en:beatrice», «en:john», «en:narrator»; `rate` — скорость речи; нет записи — лучший голос пола персонажа.
 * `speaking` — кто и что говорит сейчас (null — тишина).
 */
class Speaker(private val context: Context) {
    private var tts: TextToSpeech? = null
    private var ready = false
    private val queued = ArrayList<() -> Unit>()
    private val defaults = HashMap<String, String?>()
    var chosen: Map<String, String> = emptyMap()
    var rate: Float = 1f
    private val talk = MutableStateFlow<Speaking?>(null)
    val speaking: StateFlow<Speaking?> = talk
    private var current = ""
    private var counter = 0
    private val handler = Handler(Looper.getMainLooper())
    private val listener = object : UtteranceProgressListener() {
        override fun onStart(utteranceId: String?) {}
        override fun onDone(utteranceId: String?) = finish(utteranceId)
        @Deprecated("Deprecated in Java")
        override fun onError(utteranceId: String?) = finish(utteranceId)
        override fun onStop(utteranceId: String?, interrupted: Boolean) = finish(utteranceId)
    }

    private fun finish(id: String?) { if (id == current) talk.value = null }

    /** Произносит текст и отмечает, кто говорит; на случай, если движок не сообщит об окончании, отметка снимается по таймеру. */
    private fun say(e: TextToSpeech, text: String, characterId: String?) {
        current = "dl${++counter}"
        val id = current
        e.setOnUtteranceProgressListener(listener)
        talk.value = Speaking(characterId, text)
        e.speak(text, TextToSpeech.QUEUE_FLUSH, null, id)
        handler.postDelayed({ finish(id) }, SAFETY_MS + text.length * SAFETY_PER_CHAR_MS)
    }

    private fun ensureEngine(): TextToSpeech = tts ?: TextToSpeech(context.applicationContext) { code ->
        ready = code == TextToSpeech.SUCCESS
        if (ready) queued.forEach { it() }
        queued.clear()
    }.also { tts = it }

    private fun whenReady(job: () -> Unit) {
        ensureEngine()
        if (ready) job() else queued.add(job)
    }

    /** Озвучивает голосом персонажа `who` (null — мисс Грейс): выбранный в настройках или лучший голос его пола (см. VoiceCast). */
    fun speak(text: String, tag: String, who: String? = null) = whenReady {
        val e = tts ?: return@whenReady
        val locale = Locale.forLanguageTag(tag)
        prepare(e, locale)
        val profile = Characters.of(who)
        val cast = VoiceCast.pick(installed(e, locale), profile, chosen["${VoiceNames.language(tag)}:${profile.id}"])
        applyVoice(e, cast.voice ?: defaults[locale.language], locale)
        e.setPitch(cast.pitch)
        e.setSpeechRate(rate)
        say(e, text, profile.id)
    }

    /** Язык ставится всегда; голос по умолчанию для языка запоминается до первой смены голоса, чтобы к нему можно было вернуться. */
    private fun prepare(e: TextToSpeech, locale: Locale) {
        e.setLanguage(locale)
        if (!defaults.containsKey(locale.language)) defaults[locale.language] = runCatching { e.voice?.name }.getOrNull()
    }

    /** Установленные голоса языка как кандидаты на роль. */
    private fun installed(e: TextToSpeech, locale: Locale): List<Cand> = runCatching { e.voices }.getOrNull().orEmpty()
        .filter { it.locale.language == locale.language && TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED !in it.features.orEmpty() }
        .map { Cand(it.name, Gender.of(it.name, it.features.orEmpty()), it.isNetworkConnectionRequired, it.locale.country == locale.country, it.quality) }
        .filter { it.gender != Gender.ANY }

    /** Пробное воспроизведение конкретного голоса из списка настроек (выбор при этом не меняется). */
    fun preview(text: String, voice: String, tag: String) = whenReady {
        val e = tts ?: return@whenReady
        prepare(e, Locale.forLanguageTag(tag))
        applyVoice(e, voice, Locale.forLanguageTag(tag))
        e.setPitch(1f)
        e.setSpeechRate(rate)
        say(e, text, null)
    }

    /** Ставит голос, если он есть у движка и подходит по языку; иначе остаётся голос по умолчанию для языка. */
    private fun applyVoice(e: TextToSpeech, name: String?, locale: Locale) {
        if (name == null) return
        val voice = runCatching { e.voices }.getOrNull()?.firstOrNull { it.name == name && it.locale.language == locale.language } ?: return
        e.setVoice(voice)
    }

    /** Все голоса движка (в том числе ещё не скачанные); список придёт, когда движок будет готов. */
    fun voices(onReady: (List<RawVoice>) -> Unit) = whenReady {
        val list = runCatching { tts?.voices }.getOrNull().orEmpty().map {
            RawVoice(it.name, it.locale.toLanguageTag(), it.quality, it.isNetworkConnectionRequired, TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED !in it.features.orEmpty(), Gender.of(it.name, it.features.orEmpty()))
        }
        onReady(list)
    }

    /** Пакет движка синтеза речи по умолчанию (например com.google.android.tts); null, если движок ещё не запущен. */
    fun engine(): String? = runCatching { tts?.defaultEngine }.getOrNull()

    /** Прервать озвучку (перед записью микрофона, чтобы динамик не попал в запись). */
    fun stop() { tts?.stop(); talk.value = null }

    fun shutdown() { handler.removeCallbacksAndMessages(null); tts?.stop(); tts?.shutdown(); tts = null; ready = false; queued.clear(); defaults.clear() }
}
