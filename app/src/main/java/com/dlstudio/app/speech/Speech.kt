package com.dlstudio.app.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

/** Озвучить текст языком темы (BCP 47, например en-US). */
typealias Speak = (String) -> Unit

/** Озвучка с номером голоса: 0 — основной (выбранный в настройках), 1 и 2 — другие голоса того же языка (реплики диалога). */
typealias SpeakAs = (String, Int) -> Unit

/**
 * Один встроенный движок синтеза речи Android на приложение: создаётся при первой озвучке, а не при каждом входе на экран.
 * `chosen` — выбранные голоса (язык → служебное имя голоса), `rate` — скорость речи; пусто — голос по умолчанию.
 */
class Speaker(private val context: Context) {
    private var tts: TextToSpeech? = null
    private var ready = false
    private val queued = ArrayList<() -> Unit>()
    private val defaults = HashMap<String, String?>()
    var chosen: Map<String, String> = emptyMap()
    var rate: Float = 1f

    private fun ensureEngine(): TextToSpeech = tts ?: TextToSpeech(context.applicationContext) { code ->
        ready = code == TextToSpeech.SUCCESS
        if (ready) queued.forEach { it() }
        queued.clear()
    }.also { tts = it }

    private fun whenReady(job: () -> Unit) {
        ensureEngine()
        if (ready) job() else queued.add(job)
    }

    /** `slot` — голос: 0 основной, 1 и 2 — другие голоса языка (предпочтительно другого пола), чтобы реплики диалога различались. */
    fun speak(text: String, tag: String, slot: Int = 0) = whenReady {
        val e = tts ?: return@whenReady
        val locale = Locale.forLanguageTag(tag)
        prepare(e, locale)
        val base = chosen[VoiceNames.language(tag)] ?: defaults[locale.language]
        applyVoice(e, if (slot <= 0) base else otherVoice(e, locale, base, slot), locale)
        e.setSpeechRate(rate)
        e.speak(text, TextToSpeech.QUEUE_FLUSH, null, "dl")
    }

    /** Язык ставится всегда; голос по умолчанию для языка запоминается до первой смены голоса, чтобы к нему можно было вернуться. */
    private fun prepare(e: TextToSpeech, locale: Locale) {
        e.setLanguage(locale)
        if (!defaults.containsKey(locale.language)) defaults[locale.language] = runCatching { e.voice?.name }.getOrNull()
    }

    /** Установленный голос языка, не совпадающий с основным: сначала другого пола, без сети, того же региона, лучшего качества. */
    private fun otherVoice(e: TextToSpeech, locale: Locale, base: String?, slot: Int): String? {
        val mine = runCatching { e.voices }.getOrNull().orEmpty().filter {
            it.locale.language == locale.language && it.name != base && TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED !in it.features.orEmpty()
        }
        val g = gender(base.orEmpty())
        val ranked = mine.sortedWith(
            compareBy({ g != 0 && gender(it.name) == g }, { it.isNetworkConnectionRequired }, { it.locale.country != locale.country }, { -it.quality }, { it.name })
        )
        return if (ranked.isEmpty()) null else ranked[(slot - 1) % ranked.size].name
    }

    /** Пол по служебному имени голоса: 1 женский, 2 мужской, 0 неизвестен. */
    private fun gender(name: String): Int = name.lowercase().let { if ("female" in it) 1 else if ("male" in it) 2 else 0 }

    /** Пробное воспроизведение конкретного голоса из списка настроек (выбор при этом не меняется). */
    fun preview(text: String, voice: String, tag: String) = whenReady {
        val e = tts ?: return@whenReady
        prepare(e, Locale.forLanguageTag(tag))
        applyVoice(e, voice, Locale.forLanguageTag(tag))
        e.setSpeechRate(rate)
        e.speak(text, TextToSpeech.QUEUE_FLUSH, null, "dl")
    }

    /** Ставит голос, если он есть у движка и подходит по языку; иначе остаётся голос по умолчанию для языка. */
    private fun applyVoice(e: TextToSpeech, name: String?, locale: Locale) {
        if (name == null) return
        val voice = runCatching { e.voices }.getOrNull()?.firstOrNull { it.name == name && it.locale.language == locale.language } ?: return
        e.setVoice(voice)
    }

    /** Все голоса движка (в том числе ещё не скачанные); список придёт, когда движок будет готов. */
    fun voices(onReady: (List<RawVoice>) -> Unit) = whenReady {
        val list = runCatching { tts?.voices }.getOrNull().orEmpty().map {
            RawVoice(it.name, it.locale.toLanguageTag(), it.quality, it.isNetworkConnectionRequired, TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED !in it.features.orEmpty())
        }
        onReady(list)
    }

    /** Пакет движка синтеза речи по умолчанию (например com.google.android.tts); null, если движок ещё не запущен. */
    fun engine(): String? = runCatching { tts?.defaultEngine }.getOrNull()

    /** Прервать озвучку (перед записью микрофона, чтобы динамик не попал в запись). */
    fun stop() { tts?.stop() }

    fun shutdown() { tts?.stop(); tts?.shutdown(); tts = null; ready = false; queued.clear(); defaults.clear() }
}
