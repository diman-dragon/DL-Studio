package com.lingvadesk.app.domain.usecase

import kotlin.math.roundToInt

data class SrsResult(
    val interval: Int,
    val ease: Float,
    val due: Long,
    val reps: Int
)

class SrsUseCase {

    fun calculateNextSrs(
        interval: Int,
        ease: Float,
        reps: Int,
        grade: Int,
        fuzz: Boolean = true
    ): SrsResult {
        var newEase = ease + (0.1 - (5 - grade) * (0.08 + (5 - grade) * 0.02)).toFloat()
        if (newEase < 1.3f) newEase = 1.3f

        val newInterval: Int
        val newReps: Int

        if (grade < 3) {
            newReps = 0
            newInterval = 1
        } else {
            newReps = reps + 1
            newInterval = when (newReps) {
                1 -> 1
                2 -> 6
                else -> (interval * newEase).roundToInt()
            }
        }

        val dayMillis = 24L * 60 * 60 * 1000
        val actualInterval = if (fuzz && newInterval > 2) {
            val randomFactor = 0.95 + Math.random() * 0.1
            (newInterval * randomFactor).roundToInt().coerceAtLeast(1)
        } else {
            newInterval
        }

        val due = System.currentTimeMillis() + (actualInterval.toLong() * dayMillis)

        return SrsResult(
            interval = actualInterval,
            ease = newEase,
            due = due,
            reps = newReps
        )
    }

    fun inferGrade(
        thinkTimeMs: Long,
        lookCount: Int,
        playCount: Int,
        wasHinted: Boolean
    ): Int {
        if (wasHinted) return 1
        if (lookCount > 3 || playCount > 2) return 2
        if (thinkTimeMs < 3000L && lookCount <= 1) return 5
        if (thinkTimeMs < 8000L && lookCount <= 2) return 4
        return 3
    }
}
