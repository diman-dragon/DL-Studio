package com.dlstudio.app.ui.components

import android.speech.SpeechRecognizer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.dlstudio.app.study.Listener
import com.dlstudio.app.study.PronounceCheck
import com.dlstudio.app.study.PronounceVerdict

/** Проверка произношения одной карточки: слушает, сравнивает с `target` и хранит фазу и услышанное. */
class PronounceState(private val listener: Listener, private val target: String, private val tag: String, private val granted: () -> Boolean) {
    var phase by mutableStateOf(PronouncePhase.IDLE); private set
    var heard by mutableStateOf(""); private set
    var askPermission: () -> Unit = {}

    fun toggle() {
        when {
            phase == PronouncePhase.LISTENING -> listener.stop()
            !listener.available() -> phase = PronouncePhase.UNAVAILABLE
            !granted() -> askPermission()
            else -> start()
        }
    }

    fun start() { heard = ""; phase = PronouncePhase.LISTENING; listener.start(tag, ::finish, ::fail) }

    fun deny() { phase = PronouncePhase.DENIED }

    fun release() = listener.release()

    private fun finish(results: List<String>) {
        heard = results.firstOrNull().orEmpty()
        phase = when (PronounceCheck.verdict(target, results)) {
            PronounceVerdict.OK -> PronouncePhase.OK
            PronounceVerdict.CLOSE -> PronouncePhase.CLOSE
            PronounceVerdict.MISS -> PronouncePhase.MISS
        }
    }

    private fun fail(code: Int) {
        phase = when (code) {
            SpeechRecognizer.ERROR_NO_MATCH, SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> PronouncePhase.NO_SPEECH
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> PronouncePhase.DENIED
            else -> PronouncePhase.UNAVAILABLE
        }
    }
}
