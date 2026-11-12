package com.dlstudio.app.study

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlin.math.max
import kotlin.math.min

/**
 * Распознавание речи Android без своих серверов. Если один способ (`RecognizerEngines`) отвечает ошибкой службы,
 * пробуется следующий; когда не помог ни один, сообщается код ошибки. Вызывать с главного потока.
 */
class Listener(private val context: Context) {
    private var rec: SpeechRecognizer? = null

    fun start(tag: String, partial: (String) -> Unit, done: (List<String>) -> Unit, failed: (Int) -> Unit) {
        release()
        val engines = RecognizerEngines.all(context)
        fun attempt(i: Int) {
            val engine = engines.getOrNull(i)
            val r = engine?.let { runCatching(it.create).getOrNull() }
            fun next(code: Int) { if (i < engines.lastIndex) attempt(i + 1) else failed(code) }
            if (engine == null || r == null) return next(NO_ENGINE)
            rec = r
            r.setRecognitionListener(ResultListener(partial, { if (rec === r) done(it) }) { code ->
                if (rec === r) { if (code in SERVICE_ERRORS) { release(); next(code) } else failed(code) }
            })
            runCatching { r.startListening(RecognitionIntent.build(context, tag, engine.offline)) }.onFailure { release(); next(SpeechRecognizer.ERROR_CLIENT) }
        }
        attempt(0)
    }

    /** Закончить запись и получить результат того, что уже сказано. */
    fun stop() { rec?.stopListening() }

    fun release() { rec?.let { rec = null; it.cancel(); it.destroy() } }

    companion object {
        const val NO_ENGINE = -1
        /** Ошибки службы (сеть, сервер, клиент, занято, лимит, нет языка/модели): другой способ может сработать. */
        private val SERVICE_ERRORS = setOf(1, 2, 4, 5, 8, 10, 11, 12, 13, 14, 15)
    }
}

/** Запрос распознавания: язык `tag`, до 5 вариантов и промежуточный текст; один и тот же для службы и для системного окна голосового ввода. */
object RecognitionIntent {
    fun build(context: Context, tag: String, offline: Boolean): Intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, tag)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, tag)
        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, offline)
        putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
    }
}

/** Один способ распознавания речи; `offline` — просить локальную модель (для остальных разрешён и интернет). */
class RecognizerEngine(val offline: Boolean, val create: () -> SpeechRecognizer)

/**
 * Способы распознавания по порядку: 1) на устройстве (Android 13+), 2) служба по умолчанию, 3) служба Google напрямую —
 * она работает, даже когда ассистентом по умолчанию выбран Gemini или другое приложение и `isRecognitionAvailable` говорит «нет».
 */
object RecognizerEngines {
    private val google = ComponentName("com.google.android.googlequicksearchbox", "com.google.android.voicesearch.serviceapi.GoogleRecognitionService")

    fun all(context: Context): List<RecognizerEngine> = buildList {
        if (Build.VERSION.SDK_INT >= 33 && SpeechRecognizer.isOnDeviceRecognitionAvailable(context)) add(RecognizerEngine(true) { SpeechRecognizer.createOnDeviceSpeechRecognizer(context) })
        if (SpeechRecognizer.isRecognitionAvailable(context)) add(RecognizerEngine(false) { SpeechRecognizer.createSpeechRecognizer(context) })
        if (runCatching { context.packageManager.getServiceInfo(google, 0) }.isSuccess) add(RecognizerEngine(false) { SpeechRecognizer.createSpeechRecognizer(context, google) })
    }
}

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

/**
 * Сравнение сказанного с эталоном: регистр, знаки и «to» в начале не важны. Берётся лучший из вариантов,
 * которые вернул распознаватель; похожесть — доля совпавших букв (расстояние Левенштейна).
 */
object PronounceCheck {
    const val OK_AT = 0.85
    const val CLOSE_AT = 0.6
    private val junk = Regex("[^\\p{L}\\p{Nd}' ]")
    private val spaces = Regex("\\s+")

    fun normalize(text: String): String = text.lowercase().replace('’', '\'').replace(junk, " ").replace(spaces, " ").trim()

    fun similarity(target: String, heard: String): Double {
        val b = normalize(heard)
        val a = normalize(target)
        val variants = listOf(a, a.removePrefix("to "))
        return variants.maxOf { v -> if (v.isEmpty() && b.isEmpty()) 1.0 else 1.0 - distance(v, b).toDouble() / max(v.length, b.length) }
    }

    /** Для каждого слова эталона: сказано ли оно (есть среди услышанных слов или почти совпало). */
    fun matchedWords(target: String, heard: String): List<Boolean> {
        val said = normalize(heard).split(' ').filter { it.isNotEmpty() }
        return target.split(spaces).filter { it.isNotEmpty() }.map { w -> normalize(w).let { n -> n.isNotEmpty() && said.any { similarity(n, it) >= 0.8 } } }
    }

    fun verdict(target: String, heard: List<String>): PronounceVerdict {
        val best = heard.maxOfOrNull { similarity(target, it) } ?: 0.0
        return when {
            best >= OK_AT -> PronounceVerdict.OK
            best >= CLOSE_AT -> PronounceVerdict.CLOSE
            else -> PronounceVerdict.MISS
        }
    }

    private fun distance(a: String, b: String): Int {
        var prev = IntArray(b.length + 1) { it }
        for (i in 1..a.length) {
            val cur = IntArray(b.length + 1).also { it[0] = i }
            for (j in 1..b.length) cur[j] = min(min(cur[j - 1], prev[j]) + 1, prev[j - 1] + if (a[i - 1] == b[j - 1]) 0 else 1)
            prev = cur
        }
        return prev[b.length]
    }
}
