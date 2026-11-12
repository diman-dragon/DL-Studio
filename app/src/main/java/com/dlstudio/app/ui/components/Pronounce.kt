package com.dlstudio.app.ui.components

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.dlstudio.app.R
import com.dlstudio.app.study.Listener
import com.dlstudio.app.study.PronounceCheck
import com.dlstudio.app.study.PronounceVerdict
import com.dlstudio.app.study.RecognitionIntent
import com.dlstudio.app.ui.theme.LocalStrings
import com.dlstudio.app.ui.theme.TextRole
import com.dlstudio.app.ui.theme.style

/** Состояние проверки произношения на карточке; FAILED — служба ответила ошибкой (код показывается), UNAVAILABLE — службы нет совсем. */
enum class PronouncePhase {
    IDLE, LISTENING, OK, CLOSE, MISS, NO_SPEECH, UNAVAILABLE, FAILED, DENIED;

    /** Итог сравнения, если он уже есть. */
    val verdict: PronounceVerdict?
        get() = when (this) { OK -> PronounceVerdict.OK; CLOSE -> PronounceVerdict.CLOSE; MISS -> PronounceVerdict.MISS; else -> null }

    /** Попытка закончена (есть итог или служба не сработала): карточку можно перевернуть. «Не расслышал» — ещё можно повторить. */
    val finished: Boolean get() = this != IDLE && this != LISTENING && this != NO_SPEECH
}

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

/** Кнопка микрофона: зелёная, а пока идёт запись — красная с белым кольцом; повторное нажатие заканчивает запись. */
@Composable
fun PronounceButton(state: PronounceState, modifier: Modifier = Modifier, size: Dp = 80.dp, enabled: Boolean = true) {
    val on = state.phase == PronouncePhase.LISTENING
    val cs = androidx.compose.material3.MaterialTheme.colorScheme
    val colors = if (on) listOf(cs.error, cs.error.copy(alpha = .75f)) else listOf(cs.primary, cs.primary.copy(alpha = .75f))
    CardActionIcon(R.drawable.ic_mic, state::toggle, modifier, colors = colors, highlight = on, enabled = enabled, size = size, iconSize = size * 0.6f, contentDescription = LocalStrings.current.pronounceSetting)
}

/** Полоска результата проверки произношения над кнопками: «Слушаю…», что услышано и оценка; в покое не показывается. */
@Composable
fun PronounceFeedback(state: PronounceState, modifier: Modifier = Modifier) {
    val s = LocalStrings.current
    val phase = state.phase
    if (phase == PronouncePhase.IDLE) return
    val cs = MaterialTheme.colorScheme
    val ok = cs.primary; val warn = cs.tertiary; val err = cs.error
    val (text, tint) = when (phase) {
        PronouncePhase.LISTENING -> (if (state.heard.isBlank()) s.pronounceListening else "${s.pronounceListening}\n${s.pronounceHeard(state.heard)}") to MaterialTheme.colorScheme.primary
        PronouncePhase.OK -> "👍 ${s.pronounceOk}\n${s.pronounceHeard(state.heard)}" to ok
        PronouncePhase.CLOSE -> "${s.pronounceClose}\n${s.pronounceHeard(state.heard)}" to warn
        PronouncePhase.MISS -> "👎 ${s.pronounceMiss}\n${s.pronounceHeard(state.heard)}" to err
        PronouncePhase.NO_SPEECH -> s.pronounceNoSpeech to warn
        PronouncePhase.UNAVAILABLE -> s.pronounceUnavailable to err
        PronouncePhase.FAILED -> s.pronounceError(state.errorCode) to err
        else -> s.pronounceDenied to err
    }
    val style = TextRole.FEEDBACK.style().copy(textAlign = TextAlign.Center, hyphens = Hyphens.None)
    Box(modifier.fillMaxWidth().padding(bottom = 8.dp).clip(RoundedCornerShape(14.dp)).background(tint.copy(alpha = .16f)).padding(horizontal = 16.dp, vertical = 10.dp), Alignment.Center) {
        FitText(text, style, Modifier.fillMaxWidth(), tint, maxLines = 3)
    }
}

/** Состояние проверки произношения для `target`; микрофон спрашивается при первом нажатии, запись снимается при уходе с карточки, запасной путь — системное окно ввода. */
@Composable
fun rememberPronounce(target: String, tag: String): PronounceState {
    val context = LocalContext.current
    val state = remember(target, tag) {
        PronounceState(Listener(context), target, tag) { ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED }
    }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { if (it) state.start() else state.deny() }
    val dialog = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { r ->
        state.dialogResult(if (r.resultCode == Activity.RESULT_OK) r.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) else null)
    }
    state.askPermission = { permission.launch(Manifest.permission.RECORD_AUDIO) }
    state.openDialog = { try { dialog.launch(RecognitionIntent.build(context, tag, false)); true } catch (e: ActivityNotFoundException) { false } }
    DisposableEffect(state) { onDispose { state.release() } }
    return state
}
