package com.dlstudio.app.content

import kotlin.random.Random

/** Проверка ответов и подготовка вопросов практики: чистая логика, без Android. */
object Answers {
    /** Слова верного ответа «мешка слов»: ответ режется по пробелам. */
    fun tokens(answer: String): List<String> = answer.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }

    /** Нормальный вид для сравнения ввода: без регистра, знаков препинания и лишних пробелов; «ё» и «е» не различаются. */
    fun normalize(text: String): String =
        text.lowercase().replace('ё', 'е').filter { it.isLetterOrDigit() || it == ' ' }.trim().replace(Regex("\\s+"), " ")

    /** Верен ли введённый ответ: совпал с `answer` или с одним из `alt`. */
    fun fillOk(item: QuizItem, typed: String): Boolean {
        val got = normalize(typed)
        return got.isNotEmpty() && (listOf(item.answer) + item.alt).any { normalize(it) == got }
    }

    /** Верна ли собранная из мешка цепочка слов. */
    fun bankOk(item: QuizItem, placed: List<String>): Boolean = placed.map(::normalize) == tokens(item.answer).map(::normalize)

    /**
     * Содержимое мешка: слова ответа и лишние слова (`options`), в одном и том же перемешанном порядке для одного вопроса.
     * Мешок никогда не лежит сразу в верном порядке (если в ответе есть разные слова).
     */
    fun bag(item: QuizItem): List<String> {
        val right = tokens(item.answer)
        val mixed = (right + item.options).shuffled(Random(item.answer.hashCode() xor item.question.hashCode()))
        return if (mixed == right && right.toSet().size > 1) mixed.reversed() else mixed
    }

    /** Правая колонка заданий «соедини пары», перемешанная одинаково при каждом показе вопроса. */
    fun shuffledRight(item: QuizItem): List<String> {
        val right = item.pairs.map { it.second }
        val mixed = right.shuffled(Random(item.question.hashCode() + right.size))
        return if (mixed == right && right.toSet().size > 1) mixed.reversed() else mixed
    }

    /** Достаточно ли чисто соединены пары: допускается одна ошибка, если пар не меньше четырёх. */
    fun matchOk(pairCount: Int, mistakes: Int): Boolean = mistakes <= if (pairCount >= 4) 1 else 0
}
