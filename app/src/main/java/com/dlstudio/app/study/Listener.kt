package com.dlstudio.app.study

import android.content.Context
import android.speech.SpeechRecognizer

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
