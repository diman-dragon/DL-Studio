package com.dlstudio.app.study

/** Всё, что показывает экран статистики. Графики идут от старого дня к сегодняшнему. */
data class StudyStats(
    val totalCards: Int = 0,
    val newCards: Int = 0,
    val studiedCards: Int = 0,
    val dueCards: Int = 0,
    val overdueCards: Int = 0,
    val matureCards: Int = 0,
    val todayReviews: Int = 0,
    val averageFaceMs: Long = 0,
    val accuracy: Int = 0,
    val streakDays: Int = 0,
    val bestStreak: Int = 0,
    val weekReviews: Int = 0,
    val prevWeekReviews: Int = 0,
    val yesterdayReviews: Int = 0,
    val yesterdayAccuracy: Int = 0,
    val reviewsByDay: List<Int> = List(StatsCalc.DAYS) { 0 },
    val intervalBuckets: List<Int> = List(4) { 0 }
)
