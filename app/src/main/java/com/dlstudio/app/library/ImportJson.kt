package com.dlstudio.app.library

import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener

/**
 * Дерево из JSON: массив узлов (или один узел) `{name, back, items:[…]}`; id не нужны. Допустимы синонимы:
 * `front` вместо `name`, `translation` вместо `back`, `decks`/`cards` вместо `items`.
 * У карточки необязательны `ipa` (синонимы `transcription`, `phonetic`) и `meanings: [{translation, explanation, example, exampleTranslation}]`;
 * без `back` главным переводом становится перевод первого значения. Узлы без названия отбрасываются.
 */
object ImportJson {
    fun decode(text: String): List<LibNode> = when (val root = JSONTokener(text).nextValue()) {
        is JSONArray -> list(root)
        is JSONObject -> listOf(node(root)).filter { it.name.isNotBlank() }
        else -> emptyList()
    }

    private fun list(array: JSONArray?): List<LibNode> {
        if (array == null) return emptyList()
        return (0 until array.length()).mapNotNull { array.optJSONObject(it) }.map { node(it) }.filter { it.name.isNotBlank() }
    }

    private fun node(o: JSONObject): LibNode {
        val meanings = MeaningsJson.read(o.optJSONArray("meanings"))
        return LibNode(
            "",
            o.optString("name").ifBlank { o.optString("front") },
            o.optString("back").ifBlank { o.optString("translation") }.ifBlank { meanings.firstOrNull()?.translation.orEmpty() },
            o.optString("hint").ifBlank { o.optString("clue") },
            o.optString("explanation").ifBlank { o.optString("definition") },
            o.optString("example").ifBlank { o.optString("exampleSentence") },
            list(o.optJSONArray("items") ?: o.optJSONArray("decks") ?: o.optJSONArray("cards")),
            o.optString("ipa").ifBlank { o.optString("transcription") }.ifBlank { o.optString("phonetic") },
            meanings
        )
    }
}
