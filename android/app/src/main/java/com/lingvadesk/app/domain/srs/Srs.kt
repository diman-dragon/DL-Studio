package com.lingvadesk.app.domain.srs

import com.lingvadesk.app.domain.model.Grade
import kotlin.math.floor
import kotlin.math.max

/** Состояние расписания карточки: то, что хранится в `cards`. */
data class SrsState(val ease: Double, val interval: Int, val reps: Int, val due: Long)

/**
 * Интервальное повторение (порт `srs/nextSrs.js`).
 *
 * - «Снова»: reps → 0, повтор через 10 минут. Первый провал оставляет 25 % интервала и снижает ease на 0,1;
 *   повторный подряд (reps уже 0) интервал не режет, ease −0,2.
 * - Новая карточка / после провала (reps 0): «знаю» — 1 день (или сохранённый интервал), «трудно» — 60 %, «легко» — +40 % (не меньше 4).
 * - reps 1: «знаю» — не меньше 6 дней и не меньше прежнего интервала +1.
 * - reps ≥ 2: «знаю» — interval × ease; «трудно» — ×1,2; «легко» — ×(ease+0,15)×1,3.
 * - `pace` — темп колоды (0,75 чаще … 1,7 гораздо реже), `fuzz` ∈ [−1; 1] — разброс до ±5 % для интервалов от 7 дней.
 */
object Srs {
    const val MINUTE = 60_000L
    const val DAY = 86_400_000L
    const val MIN_EASE = 1.3
    const val FUZZ_FROM_DAYS = 7
    const val FUZZ_SHARE = 0.05

    /** Округление «как Math.round в JS»: половинки — вверх. */
    fun roundHalfUp(x: Double): Int = floor(x + 0.5).toInt()

    fun next(card: SrsState, grade: Grade, now: Long, pace: Double = 1.0, fuzz: Double = 0.0): SrsState {
        if (grade == Grade.AGAIN) {
            val repeated = card.reps == 0
            val ease = max(MIN_EASE, card.ease - if (repeated) 0.2 else 0.1)
            val interval = if (repeated) card.interval else roundHalfUp(card.interval * 0.25)
            return SrsState(ease, interval, 0, now + 10 * MINUTE)
        }

        val ease = max(
            MIN_EASE,
            card.ease + when (grade) {
                Grade.HARD -> -0.15
                Grade.EASY -> 0.15
                else -> 0.0
            }
        )

        var interval = if (card.reps < 2) {
            val good = if (card.reps == 0) max(1, card.interval) else max(6, card.interval + 1)
            when (grade) {
                Grade.HARD -> max(1, good * 6 / 10)
                Grade.EASY -> max(4, (good * 14 + 5) / 10)
                else -> good
            }
        } else {
            when (grade) {
                Grade.HARD -> max(1, roundHalfUp(card.interval * 1.2))
                Grade.EASY -> max(1, roundHalfUp(card.interval * ease * 1.3))
                else -> max(1, roundHalfUp(card.interval * card.ease))
            }
        }

        if (pace != 1.0) interval = max(1, roundHalfUp(interval * pace))
        if (fuzz != 0.0 && interval >= FUZZ_FROM_DAYS) {
            interval += roundHalfUp(interval * FUZZ_SHARE * fuzz)
        }
        return SrsState(ease, interval, card.reps + 1, now + interval * DAY)
    }
}
