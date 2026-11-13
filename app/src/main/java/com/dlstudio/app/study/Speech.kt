package com.dlstudio.app.study

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import com.dlstudio.app.library.LibNode
import java.util.Locale
import kotlin.random.Random
import kotlinx.coroutines.delay

/** Озвучить текст выбранным голосом. */
typealias Speak = (String) -> Unit

/** Что произносит диктор и что нужно сказать в режимах голоса: в «Повторе» — предложение-пример (если есть, в половине случаев) или слово. */
object SpeakTarget {
    fun of(card: LibNode, mode: CardMode, index: Int): String =
        if (mode == CardMode.REPEAT && card.example.isNotBlank() && Random(card.id.hashCode() * 17L + index).nextBoolean()) card.example else card.name
}

/** Один движок озвучки на приложение: создаётся при первой озвучке, а не при каждом входе на экран. */
class Speaker(private val context: Context) {
    private var tts: TextToSpeech? = null
    private var ready = false
    private var queued: (() -> Unit)? = null

    private fun engine(): TextToSpeech = tts ?: TextToSpeech(context.applicationContext) { code ->
        ready = code == TextToSpeech.SUCCESS
        if (ready) queued?.invoke()
        queued = null
    }.also { tts = it }

    /** `slow` — темп и паузы «помедленнее» и с паузами между фразами (см. `SlowSpeech`). */
    fun speak(text: String, voiceName: String?, slow: SlowSpeech.Style? = null) {
        val e = engine()
        val run = {
            e.setSpeechRate(slow?.rate ?: 1f); e.setPitch(1f)
            voiceName?.let { n -> e.voices?.firstOrNull { it.name == n }?.let(e::setVoice) }
            if (slow != null) SlowSpeech.parts(text, slow.pause).forEachIndexed { i, p ->
                e.speak(p.text, if (i == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD, null, "dl$i")
                if (p.pauseMs > 0) e.playSilentUtterance(p.pauseMs.toLong(), TextToSpeech.QUEUE_ADD, "dl$i-pause")
            } else e.speak(text, TextToSpeech.QUEUE_FLUSH, null, "dl")
            Unit
        }
        if (ready) run() else queued = run
    }

    /** Голоса, не требующие сети; ждёт запуска движка (вызывать не на главном потоке). */
    suspend fun voices(): List<Voice> {
        val e = engine()
        repeat(WAIT_STEPS) { if (!ready) delay(WAIT_MS) }
        return if (ready) e.voices?.filter { !it.isNetworkConnectionRequired }.orEmpty() else emptyList()
    }

    fun preview(voice: Voice, text: String) {
        val e = engine()
        e.setSpeechRate(1f); e.setPitch(1f); e.setVoice(voice)
        e.speak(text, TextToSpeech.QUEUE_FLUSH, null, "preview")
    }

    fun shutdown() { tts?.stop(); tts?.shutdown(); tts = null; ready = false }

    private companion object { const val WAIT_STEPS = 30; const val WAIT_MS = 100L }
}

/** Язык распознавания: берётся из выбранного голоса озвучки («en-us-x-…» → en-US), иначе по буквам слова. */
object SpeechLang {
    fun fromVoice(voiceName: String?): String? =
        voiceName?.substringBefore("-x-", "")?.takeIf { it.length >= 2 }?.let { Locale.forLanguageTag(it).toLanguageTag() }

    fun forText(text: String, voiceTag: String?): String =
        voiceTag ?: if (text.any { it in 'а'..'я' || it in 'А'..'Я' || it == 'ё' || it == 'Ё' }) "ru-RU" else "en-US"
}

/**
 * Медленная озвучка: текст читается целиком и естественно (одним вызовом, без нарезки на слова и группы), просто с меньшим темпом.
 * Единственное добавление — пауза между предложениями, если в тексте их несколько; внутри предложения (в том числе у запятых) движок сам расставляет паузы.
 */
object SlowSpeech {
    private const val SENTENCE_PAUSE = 500
    private val SENTENCE_END = Regex("(?<=[.!?…])\\s+")

    /** `rate` — доля обычного темпа (0,7 = на 30% медленнее), `pause` — множитель паузы между предложениями (1 = как задумано, 0 — без пауз). */
    data class Style(val rate: Float, val pause: Float)

    data class Part(val text: String, val pauseMs: Int)

    /** Фраза для проверки настроек на языке выбранного голоса. */
    fun sample(voiceTag: String?): String =
        if (voiceTag?.startsWith("ru") == true) "Я хочу чашку кофе, пожалуйста." else "I would like a cup of coffee, please."

    fun parts(text: String, pause: Float = 1f): List<Part> {
        val sentences = if (pause <= 0f) listOf(text) else text.split(SENTENCE_END).map { it.trim() }.filter { it.isNotEmpty() }
        if (sentences.isEmpty()) return listOf(Part(text, 0))
        return sentences.mapIndexed { i, sentence -> Part(sentence, if (i < sentences.lastIndex) (SENTENCE_PAUSE * pause).toInt() else 0) }
    }
}
