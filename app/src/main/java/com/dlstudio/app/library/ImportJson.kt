package com.dlstudio.app.library

import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener

/**
 * Дерево из JSON: массив узлов (или один узел) `{name, back, items:[…]}`; id не нужны. Допустимы синонимы:
 * `front` вместо `name`, `translation` вместо `back`, `decks`/`cards` вместо `items`. Узлы без названия отбрасываются.
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

    private fun node(o: JSONObject): LibNode = LibNode(
        "",
        o.optString("name").ifBlank { o.optString("front") },
        o.optString("back").ifBlank { o.optString("translation") },
        list(o.optJSONArray("items") ?: o.optJSONArray("decks") ?: o.optJSONArray("cards"))
    )
}
