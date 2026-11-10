package com.dlstudio.app.library

import org.json.JSONArray
import org.json.JSONObject

/** Значения слова ⇄ JSON: в базе лежат одной строкой в колонке `meanings`, в файле импорта — массивом `meanings`. Пустые значения отбрасываются. */
object MeaningsJson {
    fun read(array: JSONArray?): List<Meaning> {
        if (array == null) return emptyList()
        return (0 until array.length()).mapNotNull { array.optJSONObject(it) }.map(::meaning).filter { it != Meaning() }
    }

    fun decode(text: String): List<Meaning> = if (text.isBlank()) emptyList() else runCatching { read(JSONArray(text)) }.getOrDefault(emptyList())

    fun encode(meanings: List<Meaning>): String = if (meanings.isEmpty()) "" else JSONArray(meanings.map(::json)).toString()

    private fun meaning(o: JSONObject) = Meaning(
        o.optString("translation").trim(), o.optString("explanation").ifBlank { o.optString("definition") }.trim(),
        o.optString("example").trim(), o.optString("exampleTranslation").ifBlank { o.optString("example_translation") }.trim()
    )

    private fun json(m: Meaning) = JSONObject().put("translation", m.translation).put("explanation", m.explanation).put("example", m.example).put("exampleTranslation", m.exampleTranslation)
}
