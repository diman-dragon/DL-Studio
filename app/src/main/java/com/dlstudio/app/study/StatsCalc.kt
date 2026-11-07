package com.dlstudio.app.study

import kotlin.math.roundToInt

/** Расчёт статистики из расписания и журнала; без доступа к базе, поэтому проверяется тестами. */
object StatsCalc {
    const val DAYS = 14
    const val MATURE_DAYS = 21

    fun compute(total: Int, states: Collection<CardState>, perDay: Map<Long, DayAgg>, activeDays: Set<Long>, today: Long): StudyStats {
        val window = (today - DAYS + 1..today).map { perDay[it] }
        val answers = window.sumOf { it?.count ?: 0 }
        val face = window.sumOf { it?.faceMs ?: 0L }
        val recalled = window.sumOf { it?.recalled ?: 0 }
        return StudyStats(
            totalCards = total,
            newCards = (total - states.size).coerceAtLeast(0),
            studiedCards = states.size,
            dueCards = states.count { it.dueDay <= today },
            overdueCards = states.count { it.dueDay < today },
            matureCards = states.count { it.intervalDays >= MATURE_DAYS },
            todayReviews = window.last()?.count ?: 0,
            averageFaceMs = if (answers == 0) 0 else face / answers,
            accuracy = if (answers == 0) 0 else (recalled * 100.0 / answers).roundToInt(),
            streakDays = Streak.count(activeDays, today),
            yesterdayReviews = window[window.lastIndex - 1]?.count ?: 0,
            yesterdayAccuracy = window[window.lastIndex - 1]?.let { if (it.count == 0) 0 else (it.recalled * 100.0 / it.count).roundToInt() } ?: 0,
            reviewsByDay = window.map { it?.count ?: 0 },
            intervalBuckets = listOf(1, 2, 7, 30).mapIndexed { i, from ->
                states.count { it.intervalDays >= from && (i == 3 || it.intervalDays < listOf(2, 7, 30)[i]) }
            }
        )
    }
}
