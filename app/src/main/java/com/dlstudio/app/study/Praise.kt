package com.dlstudio.app.study

/** Выбор похвалы по статистике: самое «заслуженное» первым; число — повторов, разница, дни серии или проценты. */
object Praise {
    const val GOOD_ACCURACY = 85
    const val MIN_STREAK = 3

    fun pick(s: StudyStats): Pair<PraiseKind, Int> {
        val earlierBest = s.reviewsByDay.dropLast(1).maxOrNull() ?: 0
        return when {
            earlierBest > 0 && s.todayReviews > earlierBest -> PraiseKind.RECORD_DAY to s.todayReviews
            s.yesterdayReviews > 0 && s.todayReviews > s.yesterdayReviews -> PraiseKind.UP_TODAY to s.todayReviews - s.yesterdayReviews
            s.streakDays >= MIN_STREAK && s.streakDays >= s.bestStreak -> PraiseKind.BEST_STREAK to s.streakDays
            s.prevWeekReviews > 0 && s.weekReviews > s.prevWeekReviews -> PraiseKind.WEEK_UP to (s.weekReviews - s.prevWeekReviews) * 100 / s.prevWeekReviews
            s.weekReviews > 0 && s.accuracy >= GOOD_ACCURACY -> PraiseKind.ACCURACY to s.accuracy
            else -> PraiseKind.KEEP to 0
        }
    }
}
