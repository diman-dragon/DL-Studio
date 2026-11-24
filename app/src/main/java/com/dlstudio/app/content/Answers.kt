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

    /** Варианты ответа в случайном порядке при каждом показе вопроса: верный не стоит всегда на одном месте (ни первым, ни последним). */
    fun order(options: List<String>, random: Random = Random): List<String> = options.shuffled(random)

    /**
     * Порядок показа пар «соедини»: номера пар для левой и правой колонки. Обе колонки перемешаны, одинаково при каждом показе вопроса;
     * по возможности ни одна не стоит «по порядку» и они не совпадают друг с другом (иначе пары лежали бы строго напротив).
     */
    fun matchOrder(item: QuizItem): Pair<List<Int>, List<Int>> {
        val n = item.pairs.size
        val plain = List(n) { it }
        val random = Random(item.question.hashCode() + n)
        var left = plain
        var right = plain
        if (n < 2) return left to right
        repeat(MIX_TRIES) {
            left = plain.shuffled(random)
            right = plain.shuffled(random)
            if (left != plain && right != plain && left != right) return left to right
        }
        return if (n == 2) plain.reversed() to plain else plain.drop(1) + plain.first() to plain.reversed()
    }

    private const val MIX_TRIES = 20

    /** Достаточно ли чисто соединены пары: допускается одна ошибка, если пар не меньше четырёх. */
    fun matchOk(pairCount: Int, mistakes: Int): Boolean = mistakes <= if (pairCount >= 4) 1 else 0
}
