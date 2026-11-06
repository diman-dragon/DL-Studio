package com.dlstudio.app.library

data class StudyStats(
    val totalCards: Int = 0, val studiedCards: Int = 0, val dueCards: Int = 0, val overdueCards: Int = 0,
    val todayReviews: Int = 0, val averageFaceMs: Long = 0, val streakDays: Int = 0,
    val reviewsByDay: List<Int> = List(14) { 0 }, val intervalBuckets: List<Int> = List(4) { 0 }
)
