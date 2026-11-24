package com.dlstudio.app.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

/** Озвучить текст языком темы (BCP 47, например en-US). */
typealias Speak = (String) -> Unit

/** Озвучка в роли: пол и номер голоса героя диалога (см. Role); основной голос — роль по умолчанию. */
typealias SpeakAs = (String, Role) -> Unit

/**
 * Один встроенный движок синтеза речи Android на приложение: создаётся при первой озвучке, а не при каждом входе на экран.
 * `chosen` — выбранные голоса: язык («en») — основной, «en:f» и «en:m» — женский и мужской голос диалогов; `rate` — скорость речи; пусто — голос по умолчанию.
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

    /** Озвучивает голосом роли: у героя с полом — голос этого пола (см. VoiceCast), у остальных — основной или другие голоса языка. */
    fun speak(text: String, tag: String, role: Role = Role()) = whenReady {
        val e = tts ?: return@whenReady
        val locale = Locale.forLanguageTag(tag)
        prepare(e, locale)
        val lang = VoiceNames.language(tag)
        val explicit = listOfNotNull(chosen["$lang:f"]?.let { Gender.F to it }, chosen["$lang:m"]?.let { Gender.M to it }).toMap()
        val cast = VoiceCast.pick(installed(e, locale), role, explicit)
        applyVoice(e, cast.voice ?: defaults[locale.language], locale)
        e.setPitch(cast.pitch)
        e.setSpeechRate(rate)
        e.speak(text, TextToSpeech.QUEUE_FLUSH, null, "dl")
    }

    /** Язык ставится всегда; голос по умолчанию для языка запоминается до первой смены голоса, чтобы к нему можно было вернуться. */
    private fun prepare(e: TextToSpeech, locale: Locale) {
        e.setLanguage(locale)
        if (!defaults.containsKey(locale.language)) defaults[locale.language] = runCatching { e.voice?.name }.getOrNull()
    }

    /** Установленные голоса языка как кандидаты на роль. */
    private fun installed(e: TextToSpeech, locale: Locale): List<Cand> = runCatching { e.voices }.getOrNull().orEmpty()
        .filter { it.locale.language == locale.language && TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED !in it.features.orEmpty() }
        .map { Cand(it.name, Gender.of(it.name, it.features.orEmpty()), it.isNetworkConnectionRequired, it.locale.country == locale.country, it.quality) }
        .filter { it.gender != Gender.ANY }

    /** Пробное воспроизведение конкретного голоса из списка настроек (выбор при этом не меняется). */
    fun preview(text: String, voice: String, tag: String) = whenReady {
        val e = tts ?: return@whenReady
        prepare(e, Locale.forLanguageTag(tag))
        applyVoice(e, voice, Locale.forLanguageTag(tag))
        e.setPitch(1f)
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
            RawVoice(it.name, it.locale.toLanguageTag(), it.quality, it.isNetworkConnectionRequired, TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED !in it.features.orEmpty(), Gender.of(it.name, it.features.orEmpty()))
        }
        onReady(list)
    }

    /** Пакет движка синтеза речи по умолчанию (например com.google.android.tts); null, если движок ещё не запущен. */
    fun engine(): String? = runCatching { tts?.defaultEngine }.getOrNull()

    /** Прервать озвучку (перед записью микрофона, чтобы динамик не попал в запись). */
    fun stop() { tts?.stop() }

    fun shutdown() { tts?.stop(); tts?.shutdown(); tts = null; ready = false; queued.clear(); defaults.clear() }
}
