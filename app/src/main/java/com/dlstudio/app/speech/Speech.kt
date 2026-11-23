package com.dlstudio.app.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

/** Озвучить текст языком темы (BCP 47, например en-US). */
typealias Speak = (String) -> Unit

/**
 * Один встроенный движок синтеза речи Android на приложение: создаётся при первой озвучке, а не при каждом входе на экран.
 * `chosen` — выбранные голоса (язык → служебное имя голоса), `rate` — скорость речи; пусто — голос по умолчанию.
 */
class Speaker(private val context: Context) {
    private var tts: TextToSpeech? = null
    private var ready = false
    private val queued = ArrayList<() -> Unit>()
    var chosen: Map<String, String> = emptyMap()
    var rate: Float = 1f

    private fun engine(): TextToSpeech = tts ?: TextToSpeech(context.applicationContext) { code ->
        ready = code == TextToSpeech.SUCCESS
        if (ready) queued.forEach { it() }
        queued.clear()
    }.also { tts = it }

    private fun whenReady(job: () -> Unit) {
        engine()
        if (ready) job() else queued.add(job)
    }

    fun speak(text: String, tag: String) = whenReady {
        val e = tts ?: return@whenReady
        val locale = Locale.forLanguageTag(tag)
        e.setLanguage(locale)
        applyVoice(e, chosen[VoiceNames.language(tag)], locale)
        e.setSpeechRate(rate)
        e.speak(text, TextToSpeech.QUEUE_FLUSH, null, "dl")
    }

    /** Пробное воспроизведение конкретного голоса из списка настроек (выбор при этом не меняется). */
    fun preview(text: String, voice: String, tag: String) = whenReady {
        val e = tts ?: return@whenReady
        e.setLanguage(Locale.forLanguageTag(tag))
        applyVoice(e, voice, Locale.forLanguageTag(tag))
        e.setSpeechRate(rate)
        e.speak(text, TextToSpeech.QUEUE_FLUSH, null, "dl")
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
            RawVoice(it.name, it.locale.toLanguageTag(), it.quality, it.isNetworkConnectionRequired, TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED !in it.features.orEmpty())
        }
        onReady(list)
    }

    /** Прервать озвучку (перед записью микрофона, чтобы динамик не попал в запись). */
    fun stop() { tts?.stop() }

    fun shutdown() { tts?.stop(); tts?.shutdown(); tts = null; ready = false; queued.clear() }
}
