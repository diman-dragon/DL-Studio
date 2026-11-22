package com.dlstudio.app.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

/** Слушает микрофон системным распознаванием речи на языке темы. Работать с ним нужно с главного потока. */
class Listener(private val context: Context) {
    private var recognizer: SpeechRecognizer? = null

    /** Слушает одну фразу. `onHeard` — варианты распознавания; `onFail` — фаза, если ничего не вышло. */
    fun start(tag: String, onHeard: (List<String>) -> Unit, onFail: (Phase) -> Unit) {
        stop()
        if (!SpeechRecognizer.isRecognitionAvailable(context)) { onFail(Phase.FAILED); return }
        val engine = SpeechRecognizer.createSpeechRecognizer(context)
        engine.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                val heard = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).orEmpty()
                if (heard.isEmpty()) onFail(Phase.NO_SPEECH) else onHeard(heard)
            }
            override fun onError(error: Int) = onFail(
                when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH, SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> Phase.NO_SPEECH
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> Phase.NO_PERMISSION
                    else -> Phase.FAILED
                }
            )
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
        recognizer = engine
        engine.startListening(intent)
    }

    fun stop() { recognizer?.cancel(); recognizer?.destroy(); recognizer = null }
}
