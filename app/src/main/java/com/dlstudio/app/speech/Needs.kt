package com.dlstudio.app.speech

import com.dlstudio.app.content.Topic
import com.dlstudio.app.content.TheoryBlocks

/**
 * Что нужно языку установленных тем. `voiceMissing` — нет ни одного скачанного голоса; `lackFemale` / `lackMale` — в диалогах тем есть женские
 * (мужские) реплики, а голоса этого пола нет; `packs` — теги языка, чей пакет распознавания можно скачать, но он ещё не скачан.
 */
data class LangNeed(val lang: String, val tags: List<String>, val voiceMissing: Boolean, val lackFemale: Boolean, val lackMale: Boolean, val packs: List<String>) {
    val pending: Boolean get() = voiceMissing || lackFemale || lackMale || packs.isNotEmpty()
    val voices: Boolean get() = voiceMissing || lackFemale || lackMale
}

/** Языки установленных тем и то, чего им не хватает для озвучки диалогов и проверки произношения. */
object Needs {
    /** Голос подходит на пол `g`, если он этого пола, назначен на него в настройках или пол неизвестен (тогда его можно назначить вручную). */
    private fun suits(v: RawVoice, g: Int, assigned: Map<String, Int>): Boolean = (assigned[v.name] ?: v.gender).let { it == g || it == Gender.ANY }

    fun of(topics: List<Topic>, voices: List<RawVoice>, packs: Map<String, PackState>, chosen: Map<String, String>): List<LangNeed> =
        topics.filter { it.tag.isNotBlank() }.groupBy { VoiceNames.language(it.tag) }.toSortedMap().map { (lang, list) ->
            val tags = list.map { it.tag }.distinct()
            val genders = TheoryBlocks.genders(list.flatMap { t -> t.theory.flatMap { it.paragraphs } })
            val mine = voices.filter { VoiceNames.language(it.tag) == lang && it.installed }
            val assigned = listOfNotNull(chosen["$lang:f"]?.let { it to Gender.F }, chosen["$lang:m"]?.let { it to Gender.M }).toMap()
            LangNeed(
                lang, tags, mine.isEmpty(),
                Gender.F in genders && mine.none { suits(it, Gender.F, assigned) },
                Gender.M in genders && mine.none { suits(it, Gender.M, assigned) },
                tags.filter { packs[it] == PackState.AVAILABLE }
            )
        }
}
