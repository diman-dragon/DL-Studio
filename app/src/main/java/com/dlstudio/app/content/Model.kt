package com.dlstudio.app.content

import org.json.JSONArray
import org.json.JSONObject

/**
 * Одна карточка: лицо и оборот; транскрипция и пример необязательны. `say` — что произносит озвучка, если оно отличается от лица
 * (например, у буквы «A a» озвучка говорит «ay»); `mic` — предлагать ли проверку произношения (у названий букв распознавание ненадёжно).
 */
data class Card(val front: String, val back: String, val ipa: String, val example: String, val say: String = "", val mic: Boolean = true)

/** Страница теории: заголовок (может быть пустым), значок-эмодзи в шапке и абзацы. Разметка абзацев — см. TheoryBlocks. */
data class TheoryPage(val heading: String, val paragraphs: List<String>, val icon: String = "")

/** Вид вопроса практики. В json — поле `t`: choice (по умолчанию), listen, speak, bank, fill, match. */
enum class QuizKind(val tag: String) {
    CHOICE("choice"), LISTEN("listen"), SPEAK("speak"), BANK("bank"), FILL("fill"), MATCH("match");

    /** Нужны ли озвучка и микрофон: такие вопросы показываются только в темах с языком озвучки. */
    val needsVoice: Boolean get() = this == LISTEN || this == SPEAK

    companion object { fun of(tag: String): QuizKind? = values().firstOrNull { it.tag == tag.ifBlank { CHOICE.tag } } }
}

/**
 * Вопрос практики. Поля зависят от вида: `answer` — верный вариант (choice, listen), фраза для произнесения (speak),
 * верный порядок слов через пробел (bank) или верный ввод (fill); `options` — варианты (choice, listen) или лишние слова в мешке (bank);
 * `say` — что озвучить (listen, speak, fill-диктант); `alt` — другие верные вводы (fill); `pairs` — пары для соединения (match); `hint` — подсказка.
 */
data class QuizItem(
    val question: String, val answer: String, val options: List<String>, val kind: QuizKind = QuizKind.CHOICE,
    val say: String = "", val alt: List<String> = emptyList(), val pairs: List<Pair<String, String>> = emptyList(), val hint: String = ""
)

/**
 * Тема = одна колода: теория, карточки и практика. `course` — название курса, `group` — метка уровня («A1»…),
 * `n` — порядок внутри курса, `tag` — язык озвучки карточек (en-US) или пусто, если озвучивать нечего.
 * `requires` — `id` тем, которые нужно пройти раньше (у курсов-графов); пусто — тема стоит на тропе без условий.
 */
data class Topic(
    val id: String, val course: String, val group: String, val n: Int, val title: String, val tag: String,
    val theory: List<TheoryPage>, val cards: List<Card>, val quiz: List<QuizItem>, val requires: List<String> = emptyList()
)

private fun JSONArray?.objects(): List<JSONObject> = if (this == null) emptyList() else List(length()) { getJSONObject(it) }
private fun JSONArray?.strings(): List<String> = if (this == null) emptyList() else List(length()) { getString(it) }

/** Чтение тем из JSON (формат описан в README). */
object TopicJson {
    private val safeId = Regex("[a-z0-9_-]{1,64}")
    private const val MAX_PAIRS = 8

    fun parse(text: String): Topic {
        val o = JSONObject(text)
        val id = o.getString("id")
        require(safeId.matches(id)) { "bad id" }
        val topic = Topic(
            id, o.optString("course"), o.optString("group"), o.optInt("n"), o.getString("title"), o.optString("tag"),
            o.optJSONArray("theory").objects().map { TheoryPage(it.optString("h"), it.optJSONArray("p").strings(), it.optString("i")) }.filter { it.paragraphs.isNotEmpty() },
            o.optJSONArray("cards").objects().map { Card(it.getString("front"), it.getString("back"), it.optString("ipa"), it.optString("example"), it.optString("say"), it.optBoolean("mic", true)) },
            o.optJSONArray("quiz").objects().mapNotNull(::quizItem),
            requires = o.optJSONArray("requires").strings().filter { safeId.matches(it) }
        )
        require(topic.theory.isNotEmpty() || topic.cards.isNotEmpty() || topic.quiz.isNotEmpty()) { "empty topic" }
        return topic
    }

    /** Вопрос практики из json; вопрос с неизвестным видом или без нужных полей отбрасывается. */
    private fun quizItem(o: JSONObject): QuizItem? {
        val kind = QuizKind.of(o.optString("t")) ?: return null
        val answer = o.optString("a")
        val options = o.optJSONArray("options").strings()
        val pairs = o.optJSONArray("pairs").pairs()
        val say = o.optString("say").ifBlank { if (kind == QuizKind.LISTEN || kind == QuizKind.SPEAK) answer else "" }
        val item = QuizItem(o.optString("q"), answer, options, kind, say, o.optJSONArray("alt").strings(), pairs, o.optString("hint"))
        val ok = when (kind) {
            QuizKind.CHOICE -> answer in options
            QuizKind.LISTEN -> answer in options && say.isNotBlank()
            QuizKind.SPEAK -> answer.isNotBlank()
            QuizKind.BANK -> Answers.tokens(answer).size >= 2
            QuizKind.FILL -> answer.isNotBlank()
            QuizKind.MATCH -> pairs.size in 2..MAX_PAIRS
        }
        return item.takeIf { ok }
    }

    private fun JSONArray?.pairs(): List<Pair<String, String>> =
        if (this == null) emptyList() else List(length()) { optJSONArray(it) }.mapNotNull { p -> if (p != null && p.length() >= 2) p.getString(0) to p.getString(1) else null }
}
