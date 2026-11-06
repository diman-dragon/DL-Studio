package com.dlstudio.app.library

import org.json.JSONArray
import org.json.JSONObject

/** Формат файла library.json: массив узлов `{id, name, back, items:[…]}`. */
object LibraryJson {
    fun encode(nodes: List<LibNode>): String = array(nodes).toString()

    fun decode(text: String): List<LibNode> = parse(JSONArray(text))

    private fun array(nodes: List<LibNode>): JSONArray {
        val result = JSONArray()
        nodes.forEach { result.put(obj(it) as Any) }
        return result
    }

    private fun obj(n: LibNode): JSONObject = JSONObject()
        .put("id", n.id).put("name", n.name).put("back", n.back)
        .put("hint", n.hint).put("explanation", n.explanation).put("example", n.example)
        .put("items", array(n.children) as Any)

    private fun parse(a: JSONArray): List<LibNode> = List(a.length()) { i ->
        val o = a.getJSONObject(i)
        LibNode(o.getString("id"), o.getString("name"), o.optString("back"), o.optString("hint"),
            o.optString("explanation"), o.optString("example"), parse(o.optJSONArray("items") ?: JSONArray()))
    }
}
