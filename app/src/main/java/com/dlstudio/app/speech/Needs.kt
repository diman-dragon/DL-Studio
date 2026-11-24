package com.dlstudio.app.speech

import com.dlstudio.app.content.Topic

/**
 * Что нужно языку установленных тем. `lackFemale` / `lackMale` — нет скачанного голоса с известным полом для учительницы / преподавателя;
 * `packs` — теги языка, чей пакет распознавания можно скачать, но он ещё не скачан.
 */
data class LangNeed(val lang: String, val tags: List<String>, val lackFemale: Boolean, val lackMale: Boolean, val packs: List<String>) {
    val voices: Boolean get() = lackFemale || lackMale
    val pending: Boolean get() = voices || packs.isNotEmpty()
}

/** Языки установленных тем и то, чего им не хватает для озвучки и проверки произношения. */
object Needs {
    fun of(topics: List<Topic>, voices: List<RawVoice>, packs: Map<String, PackState>): List<LangNeed> =
        topics.filter { it.tag.isNotBlank() }.groupBy { VoiceNames.language(it.tag) }.toSortedMap().map { (lang, list) ->
            val tags = list.map { it.tag }.distinct()
            val mine = voices.filter { VoiceNames.language(it.tag) == lang && it.installed }
            LangNeed(lang, tags, mine.none { it.gender == Gender.F }, mine.none { it.gender == Gender.M }, tags.filter { packs[it] == PackState.AVAILABLE })
        }
}
