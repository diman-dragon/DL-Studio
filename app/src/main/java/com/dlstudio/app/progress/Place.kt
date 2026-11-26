package com.dlstudio.app.progress

import org.json.JSONObject

/**
 * Где ученик остановился в теме: этап (`stage` — номер среди этапов темы), страница теории и сколько на ней прокручено, карточка,
 * вопрос практики (номер в теме) и прокрутка экрана вопроса. `-1` — не запоминалось. Это положение, а не прогресс: очков и этапов оно не меняет.
 */
data class Place(val stage: Int = -1, val page: Int = 0, val pageScroll: Int = 0, val card: Int = -1, val question: Int = -1, val questionScroll: Int = 0) {
    companion object {
        /** Особое значение этапа: открыть тему сразу на практике (из «Повторения»). */
        const val PRACTICE = 99
    }
}

/** Места по темам в одну JSON-строку и обратно; испорченная строка даёт пустой результат. */
object PlaceJson {
    fun encode(all: Map<String, Place>): String = JSONObject().also { o ->
        all.forEach { (id, p) -> o.put(id, JSONObject().put("s", p.stage).put("p", p.page).put("ps", p.pageScroll).put("c", p.card).put("q", p.question).put("qs", p.questionScroll)) }
    }.toString()

    fun decode(text: String?): Map<String, Place> = runCatching {
        val o = JSONObject(text!!)
        o.keys().asSequence().associateWith { id ->
            o.getJSONObject(id).let { Place(it.getInt("s"), it.getInt("p"), it.getInt("ps"), it.getInt("c"), it.getInt("q"), it.getInt("qs")) }
        }
    }.getOrDefault(emptyMap())
}
