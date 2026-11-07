package com.dlstudio.app.study

import java.util.Locale

/** Язык распознавания: берётся из выбранного голоса озвучки («en-us-x-…» → en-US), иначе по буквам слова. */
object SpeechLang {
    fun fromVoice(voiceName: String?): String? =
        voiceName?.substringBefore("-x-", "")?.takeIf { it.length >= 2 }?.let { Locale.forLanguageTag(it).toLanguageTag() }

    fun forText(text: String, voiceTag: String?): String =
        voiceTag ?: if (text.any { it in 'а'..'я' || it in 'А'..'Я' || it == 'ё' || it == 'Ё' }) "ru-RU" else "en-US"
}
