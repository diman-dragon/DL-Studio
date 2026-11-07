package com.dlstudio.app.study

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

/** Встроенное в Android распознавание речи (`SpeechRecognizer`): на устройстве, если доступно, без своих серверов. Вызывать с главного потока. */
class Listener(private val context: Context) {
    private var rec: SpeechRecognizer? = null

    fun available(): Boolean = SpeechRecognizer.isRecognitionAvailable(context) ||
        (Build.VERSION.SDK_INT >= 33 && SpeechRecognizer.isOnDeviceRecognitionAvailable(context))

    private fun create(): SpeechRecognizer =
        if (Build.VERSION.SDK_INT >= 33 && SpeechRecognizer.isOnDeviceRecognitionAvailable(context)) SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
        else SpeechRecognizer.createSpeechRecognizer(context)

    fun start(tag: String, done: (List<String>) -> Unit, failed: (Int) -> Unit) {
        release()
        val r = create()
        r.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) { if (rec === r) done(results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).orEmpty()) }
            override fun onError(error: Int) { if (rec === r) failed(error) }
            override fun onReadyForSpeech(params: Bundle?) = Unit
            override fun onBeginningOfSpeech() = Unit
            override fun onRmsChanged(rmsdB: Float) = Unit
            override fun onBufferReceived(buffer: ByteArray?) = Unit
            override fun onEndOfSpeech() = Unit
            override fun onPartialResults(partialResults: Bundle?) = Unit
            override fun onEvent(eventType: Int, params: Bundle?) = Unit
        })
        rec = r
        r.startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, tag)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
        })
    }

    /** Закончить запись и получить результат того, что уже сказано. */
    fun stop() { rec?.stopListening() }

    fun release() { rec?.let { rec = null; it.cancel(); it.destroy() } }
}
