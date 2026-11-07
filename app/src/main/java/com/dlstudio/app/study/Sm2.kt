package com.dlstudio.app.study

import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Расписание повторов по SM-2 с тремя оценками. Интервалы целые, в днях:
 * 1 → 6 → ×ease. «Не знал» сбрасывает карточку и оставляет её на сегодня;
 * повтор раньше срока (практика) расписание не двигает, чтобы интервал нельзя было накрутить.
 */
object Sm2 {
    const val START_EASE = 2.5
    const val MIN_EASE = 1.3
    const val MAX_EASE = 2.8
    const val MAX_INTERVAL = 365

    private fun ease(value: Double) = value.coerceIn(MIN_EASE, MAX_EASE)

    private fun grown(prev: Int, factor: Double) = min(MAX_INTERVAL, max(prev + 1, (prev * factor).roundToInt()))

    fun next(prev: CardState?, grade: Grade, today: Long): CardState {
        val old = prev ?: CardState(0, START_EASE, 0, 0, today, today)
        if (grade == Grade.AGAIN) {
            val lapses = old.lapses + if (old.reps > 0) 1 else 0
            return CardState(1, ease(old.ease - 0.2), 0, lapses, today, today)
        }
        if (old.reps > 0 && today < old.dueDay) return old.copy(lastDay = today)
        val miss = 5 - grade.quality
        val ease = ease(old.ease + 0.1 - miss * (0.08 + miss * 0.02))
        val interval = when {
            old.reps == 0 -> 1
            old.reps == 1 -> if (grade == Grade.HARD) 3 else 6
            else -> grown(old.intervalDays, if (grade == Grade.HARD) 1.2 else ease)
        }
        return CardState(interval, ease, old.reps + 1, old.lapses, today + interval, today)
    }
}
