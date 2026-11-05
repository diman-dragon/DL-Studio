package com.lingvadesk.app.platform

import android.content.Context
import android.speech.tts.TextToSpeech
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/** Озвучка системным TextToSpeech (замена плагинов TtsSpeak/TtsStop). Движок создаётся при первом обращении. */
@Singleton
class TtsEngine @Inject constructor(@ApplicationContext private val ctx: Context) {
    private var tts: TextToSpeech? = null
    private var ready = false
    private val queue = ArrayList<(TextToSpeech) -> Unit>()
    private var seq = 0

    private fun withEngine(block: (TextToSpeech) -> Unit) {
        val e = tts
        if (e != null && ready) { block(e); return }
        synchronized(queue) { queue += block }
        if (e == null) {
            tts = TextToSpeech(ctx.applicationContext) { status ->
                ready = status == TextToSpeech.SUCCESS
                val pending = synchronized(queue) { queue.toList().also { queue.clear() } }
                if (ready) tts?.let { eng -> pending.forEach { it(eng) } }
            }
        }
    }

    private fun prepare(e: TextToSpeech, lang: String, rate: Float): Boolean {
        val r = e.setLanguage(Locale.forLanguageTag(lang))
        if (r == TextToSpeech.LANG_MISSING_DATA || r == TextToSpeech.LANG_NOT_SUPPORTED) return false
        e.setSpeechRate(rate.coerceIn(0.3f, 2.5f))
        return true
    }

    fun speak(text: String, lang: String, rate: Float = 1f) {
        if (text.isBlank()) return
        withEngine { e ->
            if (prepare(e, lang, rate)) e.speak(text, TextToSpeech.QUEUE_FLUSH, null, "lv${++seq}")
        }
    }

    /** Повтор по словам с паузой между ними (0,8 с — «медленно», 1,8 с — «ещё медленнее»). */
    fun speakSlow(text: String, lang: String, gapMs: Long, rate: Float = 1f) {
        val words = text.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
        if (words.isEmpty()) return
        withEngine { e ->
            if (!prepare(e, lang, rate)) return@withEngine
            e.stop()
            words.forEachIndexed { i, w ->
                e.speak(w, TextToSpeech.QUEUE_ADD, null, "lv${++seq}")
                if (i < words.lastIndex) e.playSilentUtterance(gapMs, TextToSpeech.QUEUE_ADD, "lv${++seq}")
            }
        }
    }

    fun stop() { tts?.takeIf { ready }?.stop() }
}
