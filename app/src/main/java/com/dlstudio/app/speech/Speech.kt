package com.dlstudio.app.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

/** Озвучить текст языком темы (BCP 47, например en-US). */
typealias Speak = (String) -> Unit

/** Один движок озвучки Android на приложение: создаётся при первой озвучке, а не при каждом входе на экран. */
class Speaker(private val context: Context) {
    private var tts: TextToSpeech? = null
    private var ready = false
    private var queued: (() -> Unit)? = null

    private fun engine(): TextToSpeech = tts ?: TextToSpeech(context.applicationContext) { code ->
        ready = code == TextToSpeech.SUCCESS
        if (ready) queued?.invoke()
        queued = null
    }.also { tts = it }

    fun speak(text: String, tag: String) {
        val e = engine()
        val run = {
            e.setLanguage(Locale.forLanguageTag(tag))
            e.setSpeechRate(1f)
            e.speak(text, TextToSpeech.QUEUE_FLUSH, null, "dl")
            Unit
        }
        if (ready) run() else queued = run
    }

    fun shutdown() { tts?.stop(); tts?.shutdown(); tts = null; ready = false }
}
