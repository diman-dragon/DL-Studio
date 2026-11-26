package com.dlstudio.app.content

import com.dlstudio.app.speech.Characters
import org.json.JSONArray
import org.json.JSONObject

/**
 * Одна карточка. У каждого поля своё место на экране, пустое поле не показывается вовсе.
 * Лицо: `front` — слово или фраза на изучаемом языке, `pos` — часть речи, `ipa` — транскрипция.
 * Оборот: `back` — перевод, `forms` — формы слова (go — went — gone), `example` — пример на изучаемом языке, `exampleTr` — его перевод, `note` — объяснение.
 * Озвучиваются и проверяются микрофоном только `front` и `example` (изучаемый язык); перевод, объяснение и остальное — никогда.
 * `say` — что произносит озвучка, если оно отличается от лица (у буквы «A a» озвучка говорит «ay»); `mic` — предлагать ли проверку произношения
 * (у названий букв распознавание ненадёжно).
 */
data class Card(
    val front: String, val back: String, val ipa: String, val example: String, val say: String = "", val mic: Boolean = true,
    val pos: String = "", val forms: String = "", val exampleTr: String = "", val note: String = ""
)

/** Страница теории: заголовок (может быть пустым), значок-эмодзи в шапке и абзацы. Разметка абзацев — см. TheoryBlocks. */
data class TheoryPage(val heading: String, val paragraphs: List<String>, val icon: String = "")

/** Вид вопроса практики. В json — поле `t`: choice (по умолчанию), listen, speak, bank, fill, match. */
enum class QuizKind(val tag: String) {
    CHOICE("choice"), LISTEN("listen"), SPEAK("speak"), BANK("bank"), FILL("fill"), MATCH("match"), GAP("gap"), FIND("find");

    /** Нужны ли озвучка и микрофон: такие вопросы показываются только в темах с языком озвучки. */
    val needsVoice: Boolean get() = this == LISTEN || this == SPEAK

    companion object { fun of(tag: String): QuizKind? = values().firstOrNull { it.tag == tag.ifBlank { CHOICE.tag } } }
}

/**
 * Вопрос практики. Поля зависят от вида: `answer` — верный вариант (choice, listen), фраза для произнесения (speak),
 * верный порядок слов через пробел (bank), верный ввод (fill), слова для пропусков `___` через пробел или `|` (gap) или слово с ошибкой, как оно написано в вопросе (find; верное написание — в `hint`); `options` — варианты (choice, listen) или лишние слова в мешке (bank, gap);
 * `say` — что озвучить (listen, speak, fill-диктант); `alt` — другие верные вводы (fill); `pairs` — пары для соединения (match); `hint` — подсказка;
 * `who` — ID персонажа, чьим голосом звучит `say` (в json поле `who`; пусто или неизвестный ID — мисс Грейс).
 */
data class QuizItem(
    val question: String, val answer: String, val options: List<String>, val kind: QuizKind = QuizKind.CHOICE,
    val say: String = "", val alt: List<String> = emptyList(), val pairs: List<Pair<String, String>> = emptyList(), val hint: String = "",
    val who: String? = null
) {
    /** Без звука такой вопрос не решить: «на слух», «произнеси» и диктант (вписать то, что услышал, когда текста вопроса нет). */
    val needsSound: Boolean get() = kind.needsVoice || (kind == QuizKind.FILL && say.isNotBlank() && question.isBlank())
}

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
            o.optJSONArray("cards").objects().map { Card(it.getString("front"), it.getString("back"), it.optString("ipa"), it.optString("example"), it.optString("say"), it.optBoolean("mic", true), it.optString("pos"), it.optString("forms"), it.optString("exampleTr"), it.optString("note")) },
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
        val who = o.optString("who").lowercase().takeIf { Characters.get(it) != null }
        val item = QuizItem(o.optString("q"), answer, options, kind, say, o.optJSONArray("alt").strings(), pairs, o.optString("hint"), who)
        val ok = when (kind) {
            QuizKind.CHOICE -> answer in options
            QuizKind.LISTEN -> answer in options && say.isNotBlank()
            QuizKind.SPEAK -> answer.isNotBlank()
            QuizKind.BANK -> Answers.tokens(answer).size >= 2
            QuizKind.FILL -> answer.isNotBlank()
            QuizKind.MATCH -> pairs.size in 2..MAX_PAIRS
            QuizKind.GAP -> Answers.gapParts(item).size > 1 && Answers.gapWords(answer).size == Answers.gapParts(item).size - 1
            QuizKind.FIND -> Answers.findIndex(item) >= 0
        }
        return item.takeIf { ok }
    }

    private fun JSONArray?.pairs(): List<Pair<String, String>> =
        if (this == null) emptyList() else List(length()) { optJSONArray(it) }.mapNotNull { p -> if (p != null && p.length() >= 2) p.getString(0) to p.getString(1) else null }
}
