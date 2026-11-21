package com.dlstudio.app.content

import org.json.JSONArray
import org.json.JSONObject

/** Одна карточка: лицо и оборот; транскрипция и пример необязательны. */
data class Card(val front: String, val back: String, val ipa: String, val example: String)

/** Страница теории: заголовок (может быть пустым) и абзацы. В тексте допустимы **жирный** и _курсив_. */
data class TheoryPage(val heading: String, val paragraphs: List<String>)

/** Вопрос практики с готовыми вариантами; правильный ответ входит в `options`. */
data class QuizItem(val question: String, val answer: String, val options: List<String>)

/**
 * Тема = одна колода: теория, карточки и практика. `course` — название курса, `group` — метка уровня («A1»…),
 * `n` — порядок внутри курса, `tag` — язык озвучки карточек (en-US) или пусто, если озвучивать нечего.
 */
data class Topic(
    val id: String, val course: String, val group: String, val n: Int, val title: String, val tag: String,
    val theory: List<TheoryPage>, val cards: List<Card>, val quiz: List<QuizItem>
)

/** Строка каталога: что скачать (`path` относительно папки каталога) и как проверить. */
data class CatalogEntry(val id: String, val course: String, val group: String, val n: Int, val title: String, val path: String, val size: Long, val sha256: String, val cards: Int)

private fun JSONArray?.objects(): List<JSONObject> = if (this == null) emptyList() else List(length()) { getJSONObject(it) }
private fun JSONArray?.strings(): List<String> = if (this == null) emptyList() else List(length()) { getString(it) }

/** Чтение тем и каталога из JSON (формат описан в README). */
object TopicJson {
    private val safeId = Regex("[a-z0-9_-]{1,64}")
    private val safePath = Regex("[A-Za-z0-9_./-]{1,128}")

    fun parse(text: String): Topic {
        val o = JSONObject(text)
        val id = o.getString("id")
        require(safeId.matches(id)) { "bad id" }
        val topic = Topic(
            id, o.optString("course"), o.optString("group"), o.optInt("n"), o.getString("title"), o.optString("tag"),
            o.optJSONArray("theory").objects().map { TheoryPage(it.optString("h"), it.optJSONArray("p").strings()) }.filter { it.paragraphs.isNotEmpty() },
            o.optJSONArray("cards").objects().map { Card(it.getString("front"), it.getString("back"), it.optString("ipa"), it.optString("example")) },
            o.optJSONArray("quiz").objects().map { QuizItem(it.getString("q"), it.getString("a"), it.optJSONArray("options").strings()) }.filter { it.answer in it.options }
        )
        require(topic.theory.isNotEmpty() || topic.cards.isNotEmpty() || topic.quiz.isNotEmpty()) { "empty topic" }
        return topic
    }

    fun parseCatalog(text: String): List<CatalogEntry> = JSONObject(text).optJSONArray("topics").objects().mapNotNull {
        val id = it.optString("id")
        val path = it.optString("pack")
        if (!safeId.matches(id) || !safePath.matches(path) || ".." in path) null
        else CatalogEntry(id, it.optString("course"), it.optString("group"), it.optInt("n"), it.optString("title", id), path, it.optLong("size"), it.optString("sha256"), it.optInt("cards"))
    }
}
