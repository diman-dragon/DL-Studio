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
 * Медленная озвучка: фраза делится на куски, между ними вставляются паузы (после запятой короче, после точки длиннее).
 * Длинный отрывок без знаков препинания делится на группы не больше четырёх слов. Темп и длина пауз задаются `Style` (из настроек);
 * при нулевых паузах фраза читается целиком, только медленнее.
 */
object SlowSpeech {
    private const val MAX_WORDS = 4
    private const val GROUP_PAUSE = 300
    private const val COMMA_PAUSE = 450
    private const val STOP_PAUSE = 800
    private val CLAUSE = Regex("[^,;:.!?…]+[,;:.!?…]*")

    /** `rate` — доля обычного темпа (0,75 = на четверть медленнее), `pause` — множитель пауз (1 = как задумано). */
    data class Style(val rate: Float, val pause: Float)

    data class Part(val text: String, val pauseMs: Int)

    /** Фраза для проверки настроек на языке выбранного голоса. */
    fun sample(voiceTag: String?): String =
        if (voiceTag?.startsWith("ru") == true) "Я хочу чашку кофе, пожалуйста." else "I would like a cup of coffee, please."

    fun parts(text: String, pause: Float = 1f): List<Part> {
        if (pause <= 0f) return listOf(Part(text, 0))
        val parts = CLAUSE.findAll(text).map { it.value.trim() }.filter { c -> c.any { it.isLetterOrDigit() } }.flatMap { clause ->
            val words = clause.split(Regex("\\s+"))
            val count = (words.size + MAX_WORDS - 1) / MAX_WORDS
            val groups = words.chunked((words.size + count - 1) / count).map { it.joinToString(" ") }
            val end = if (clause.last() in ".!?…") STOP_PAUSE else COMMA_PAUSE
            groups.mapIndexed { i, g -> Part(g, ((if (i < groups.lastIndex) GROUP_PAUSE else end) * pause).toInt()) }
        }.toList()
        return if (parts.isEmpty()) listOf(Part(text, 0)) else parts.dropLast(1) + parts.last().copy(pauseMs = 0)
    }
}
