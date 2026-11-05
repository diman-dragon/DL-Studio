package com.lingvadesk.app.domain.srs

import kotlin.math.max
import kotlin.math.min

/** Сходство услышанного с эталоном (порт `study/scoreSpeech.js`): лучший из вариантов распознавания, 0…1. */
object SpeechScore {
    private val NOT_WORD = Regex("[^\\p{L}\\p{N}\\s]")
    private val SPACES = Regex("\\s+")

    fun normalize(s: String): String =
        NOT_WORD.replace(s.lowercase(), "").let { SPACES.replace(it, " ") }.trim()

    fun levenshtein(a: String, b: String): Int {
        if (a == b) return 0
        if (a.isEmpty()) return b.length
        if (b.isEmpty()) return a.length
        var prev = IntArray(b.length + 1) { it }
        var cur = IntArray(b.length + 1)
        for (i in 1..a.length) {
            cur[0] = i
            for (j in 1..b.length) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                cur[j] = min(min(cur[j - 1] + 1, prev[j] + 1), prev[j - 1] + cost)
            }
            val t = prev; prev = cur; cur = t
        }
        return prev[b.length]
    }

    fun similarity(heard: String, target: String): Double {
        val a = normalize(heard)
        val b = normalize(target)
        if (a.isEmpty() || b.isEmpty()) return 0.0
        return 1.0 - levenshtein(a, b).toDouble() / max(a.length, b.length)
    }

    /** `confidence` ≤ 0 — уверенность неизвестна, не штрафуем. */
    fun score(text: String, alternatives: List<String>, confidence: Double, target: String): Double {
        val best = (listOf(text) + alternatives).maxOfOrNull { similarity(it, target) } ?: 0.0
        if (best <= 0.0) return 0.0
        val adjusted = if (confidence > 0.0 && confidence < 1.0) best - (1.0 - confidence) * 0.15 else best
        return Math.round(adjusted.coerceIn(0.0, 1.0) * 100.0) / 100.0
    }
}
