package com.dlstudio.app.study

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import kotlinx.coroutines.delay

/** Один движок озвучки на приложение: создаётся при первой озвучке, а не при каждом входе на экран. */
class Speaker(private val context: Context) {
    private var tts: TextToSpeech? = null
    private var ready = false
    private var queued: (() -> Unit)? = null

    private fun engine(): TextToSpeech = tts ?: TextToSpeech(context.applicationContext) { code ->
        ready = code == TextToSpeech.SUCCESS
        if (ready) queued?.invoke()
        queued = null
    }.also { tts = it }

    fun speak(text: String, rate: Float, pitch: Float, voiceName: String?) {
        val e = engine()
        val run = {
            e.setSpeechRate(rate); e.setPitch(pitch)
            voiceName?.let { n -> e.voices?.firstOrNull { it.name == n }?.let(e::setVoice) }
            e.speak(text, TextToSpeech.QUEUE_FLUSH, null, "dl")
            Unit
        }
        if (ready) run() else queued = run
    }

    /** Голоса, не требующие сети; ждёт запуска движка (вызывать не на главном потоке). */
    suspend fun voices(): List<Voice> {
        val e = engine()
        repeat(WAIT_STEPS) { if (!ready) delay(WAIT_MS) }
        return if (ready) e.voices?.filter { !it.isNetworkConnectionRequired }.orEmpty() else emptyList()
    }

    fun preview(voice: Voice, text: String, rate: Float, pitch: Float) {
        val e = engine()
        e.setSpeechRate(rate); e.setPitch(pitch); e.setVoice(voice)
        e.speak(text, TextToSpeech.QUEUE_FLUSH, null, "preview")
    }

    fun shutdown() { tts?.stop(); tts?.shutdown(); tts = null; ready = false }

    private companion object { const val WAIT_STEPS = 30; const val WAIT_MS = 100L }
}
