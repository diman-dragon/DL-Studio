package com.dlstudio.app.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

/** Коды ошибок распознавателя Android 12+ (SpeechRecognizer.ERROR_LANGUAGE_*): язык не поддерживается / пакет языка не скачан. */
private const val ERROR_LANGUAGE_NOT_SUPPORTED = 12
private const val ERROR_LANGUAGE_UNAVAILABLE = 13

/**
 * Слушает микрофон встроенным распознаванием речи Android на языке темы; если на устройстве скачан языковой пакет, работает без сети.
 * Работать с ним нужно с главного потока.
 */
class Listener(private val context: Context) {
    private var recognizer: SpeechRecognizer? = null

    /** Слушает одну фразу. `onHeard` — варианты распознавания; `onFail` — фаза, если ничего не вышло. */
    fun start(tag: String, onHeard: (List<String>) -> Unit, onFail: (Phase) -> Unit, offline: Boolean = true) {
        stop()
        if (!SpeechRecognizer.isRecognitionAvailable(context)) { onFail(Phase.FAILED); return }
        val engine = SpeechRecognizer.createSpeechRecognizer(context)
        engine.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                val heard = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).orEmpty()
                if (heard.isEmpty()) onFail(Phase.NO_SPEECH) else onHeard(heard)
            }
            override fun onError(error: Int) {
                val noPack = error == ERROR_LANGUAGE_NOT_SUPPORTED || error == ERROR_LANGUAGE_UNAVAILABLE
                if (noPack && offline) start(tag, onHeard, onFail, false) else onFail(phaseOf(error))
            }
            override fun onReadyForSpeech(params: Bundle?) = Unit
            override fun onBeginningOfSpeech() = Unit
            override fun onRmsChanged(rmsdB: Float) = Unit
            override fun onBufferReceived(buffer: ByteArray?) = Unit
            override fun onEndOfSpeech() = Unit
            override fun onPartialResults(partialResults: Bundle?) = Unit
            override fun onEvent(eventType: Int, params: Bundle?) = Unit
        })
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE, tag)
            .putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            .putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, offline)
        recognizer = engine
        engine.startListening(intent)
    }

    private fun phaseOf(error: Int): Phase = when (error) {
        SpeechRecognizer.ERROR_NO_MATCH, SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> Phase.NO_SPEECH
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> Phase.NO_PERMISSION
        ERROR_LANGUAGE_NOT_SUPPORTED, ERROR_LANGUAGE_UNAVAILABLE -> Phase.NO_PACK
        else -> Phase.FAILED
    }

    fun stop() { recognizer?.cancel(); recognizer?.destroy(); recognizer = null }
}
