package com.dlstudio.app.speech

import kotlin.math.roundToInt

/** Этап проверки произношения: ждём, слушаем, получили оценку или не вышло (NO_PACK — для языка нет пакета распознавания речи). */
enum class Phase { IDLE, LISTENING, DONE, NO_SPEECH, NO_PERMISSION, NO_PACK, FAILED }

/** Оценка: насколько сказанное похоже на нужную фразу. */
enum class Grade { GREAT, GOOD, RETRY }

/** Состояние проверки: для какой фразы (`text`), что услышали (`heard`) и сколько процентов совпало (`score`). */
data class Pronounce(val text: String = "", val phase: Phase = Phase.IDLE, val heard: String = "", val score: Int = 0)

/** Проверка произношения, как её видит экран: состояние и действия (начать слушать, отменить, нет доступа к микрофону, скачать пакет распознавания языка темы). */
class Check(val state: Pronounce, val start: (String) -> Unit, val cancel: () -> Unit, val denied: (String) -> Unit, val downloadPack: () -> Unit)

/** Сравнение сказанного с фразой: чистая логика, без Android. Оценивается то, что распознал системный движок речи. */
object Pronunciation {
    private const val GREAT_FROM = 85
    private const val GOOD_FROM = 60

    fun normalize(text: String): String =
        text.lowercase().filter { it.isLetterOrDigit() || it == ' ' }.trim().replace(Regex("\\s+"), " ")

    /** Лучшее совпадение (0..100) среди вариантов, которые вернул распознаватель. */
    fun score(expected: String, heard: List<String>): Int {
        val wanted = normalize(expected)
        if (wanted.isEmpty()) return 0
        val best = heard.maxOfOrNull { similarity(wanted, normalize(it)) } ?: 0.0
        return (best * 100).roundToInt()
    }

    fun grade(score: Int): Grade = when {
        score >= GREAT_FROM -> Grade.GREAT
        score >= GOOD_FROM -> Grade.GOOD
        else -> Grade.RETRY
    }

    private fun similarity(a: String, b: String): Double {
        val longest = maxOf(a.length, b.length)
        return if (longest == 0) 0.0 else 1.0 - distance(a, b).toDouble() / longest
    }

    /** Расстояние Левенштейна: сколько правок превращают одну строку в другую. */
    private fun distance(a: String, b: String): Int {
        var row = IntArray(b.length + 1) { it }
        for (i in 1..a.length) {
            val next = IntArray(b.length + 1)
            next[0] = i
            for (j in 1..b.length) next[j] = minOf(row[j] + 1, next[j - 1] + 1, row[j - 1] + if (a[i - 1] == b[j - 1]) 0 else 1)
            row = next
        }
        return row[b.length]
    }
}
