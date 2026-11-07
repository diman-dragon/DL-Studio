package com.dlstudio.app.study

import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer

/** Слушатель `SpeechRecognizer`: передаёт дальше промежуточный текст, итоговые варианты и код ошибки; пустой итог — «не расслышал» (7). */
class ResultListener(private val partial: (String) -> Unit, private val done: (List<String>) -> Unit, private val failed: (Int) -> Unit) : RecognitionListener {
    private fun texts(b: Bundle?) = b?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).orEmpty()

    override fun onResults(results: Bundle?) { texts(results).let { if (it.isEmpty()) failed(SpeechRecognizer.ERROR_NO_MATCH) else done(it) } }
    override fun onPartialResults(partialResults: Bundle?) { texts(partialResults).firstOrNull()?.let(partial) }
    override fun onError(error: Int) = failed(error)
    override fun onReadyForSpeech(params: Bundle?) = Unit
    override fun onBeginningOfSpeech() = Unit
    override fun onRmsChanged(rmsdB: Float) = Unit
    override fun onBufferReceived(buffer: ByteArray?) = Unit
    override fun onEndOfSpeech() = Unit
    override fun onEvent(eventType: Int, params: Bundle?) = Unit
}
