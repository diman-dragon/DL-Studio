package com.dlstudio.app.ui.components

import android.speech.SpeechRecognizer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.dlstudio.app.study.Listener
import com.dlstudio.app.study.PronounceCheck
import com.dlstudio.app.study.PronounceVerdict

/**
 * Проверка произношения одной карточки: слушает, сравнивает с `target` и хранит фазу и услышанное (в том числе промежуточное).
 * Если встроенные службы не сработали, открывает системное окно голосового ввода (`openDialog`), как это делают Google и Gemini.
 */
class PronounceState(private val listener: Listener, private val target: String, private val tag: String, private val granted: () -> Boolean) {
    var phase by mutableStateOf(PronouncePhase.IDLE); private set
    var heard by mutableStateOf(""); private set
    var errorCode by mutableStateOf(0); private set
    var askPermission: () -> Unit = {}
    var openDialog: () -> Boolean = { false }

    fun toggle() = when {
        phase == PronouncePhase.LISTENING -> listener.stop()
        !granted() -> askPermission()
        else -> start()
    }

    fun start() { heard = ""; phase = PronouncePhase.LISTENING; listener.start(tag, { heard = it }, ::finish, ::fail) }

    fun deny() { phase = PronouncePhase.DENIED }

    /** Ответ системного окна голосового ввода: варианты текста или `null`, если окно закрыли. */
    fun dialogResult(results: List<String>?) { if (results.isNullOrEmpty()) phase = PronouncePhase.IDLE else finish(results) }

    fun release() = listener.release()

    private fun finish(results: List<String>) {
        heard = results.first()
        phase = when (PronounceCheck.verdict(target, results)) {
            PronounceVerdict.OK -> PronouncePhase.OK
            PronounceVerdict.CLOSE -> PronouncePhase.CLOSE
            PronounceVerdict.MISS -> PronouncePhase.MISS
        }
    }

    private fun fail(code: Int) {
        errorCode = code
        phase = when {
            code == SpeechRecognizer.ERROR_NO_MATCH || code == SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> PronouncePhase.NO_SPEECH
            code == SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> PronouncePhase.DENIED
            openDialog() -> PronouncePhase.LISTENING
            code == Listener.NO_ENGINE -> PronouncePhase.UNAVAILABLE
            else -> PronouncePhase.FAILED
        }
    }
}
