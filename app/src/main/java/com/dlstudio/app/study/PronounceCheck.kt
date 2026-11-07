package com.dlstudio.app.study

import kotlin.math.max
import kotlin.math.min

/**
 * Сравнение сказанного с эталоном: регистр, знаки и «to» в начале не важны. Берётся лучший из вариантов,
 * которые вернул распознаватель; похожесть — доля совпавших букв (расстояние Левенштейна).
 */
object PronounceCheck {
    const val OK_AT = 0.85
    const val CLOSE_AT = 0.6
    private val junk = Regex("[^\\p{L}\\p{Nd}' ]")
    private val spaces = Regex("\\s+")

    fun normalize(text: String): String = text.lowercase().replace('’', '\'').replace(junk, " ").replace(spaces, " ").trim()

    fun similarity(target: String, heard: String): Double {
        val b = normalize(heard)
        val a = normalize(target)
        val variants = listOf(a, a.removePrefix("to "))
        return variants.maxOf { v -> if (v.isEmpty() && b.isEmpty()) 1.0 else 1.0 - distance(v, b).toDouble() / max(v.length, b.length) }
    }

    fun verdict(target: String, heard: List<String>): PronounceVerdict {
        val best = heard.maxOfOrNull { similarity(target, it) } ?: 0.0
        return when {
            best >= OK_AT -> PronounceVerdict.OK
            best >= CLOSE_AT -> PronounceVerdict.CLOSE
            else -> PronounceVerdict.MISS
        }
    }

    private fun distance(a: String, b: String): Int {
        var prev = IntArray(b.length + 1) { it }
        for (i in 1..a.length) {
            val cur = IntArray(b.length + 1).also { it[0] = i }
            for (j in 1..b.length) cur[j] = min(min(cur[j - 1], prev[j]) + 1, prev[j - 1] + if (a[i - 1] == b[j - 1]) 0 else 1)
            prev = cur
        }
        return prev[b.length]
    }
}
