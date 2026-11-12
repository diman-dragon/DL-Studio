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

/** Уровень похвалы за сессию: 0 — поддержка, 1 — «хорошо», 2 — «отлично» (знал 80% и больше). */
object SessionPraise {
    fun level(know: Int, total: Int): Int = when {
        total == 0 -> 0
        know * 100 >= total * 80 -> 2
        know * 100 >= total * 50 -> 1
        else -> 0
    }
}

/** Выбор мотивационного сообщения по статистике; число — серия, просроченные или выученные (если сообщению оно нужно). */
object Motivation {
    fun pick(s: StudyStats): Pair<MotivationKind, Int> = when {
        s.totalCards == 0 -> MotivationKind.EMPTY to 0
        s.studiedCards == 0 -> MotivationKind.START to 0
        s.todayReviews > 0 && s.dueCards == 0 -> MotivationKind.DONE_TODAY to s.todayReviews
        s.todayReviews == 0 && s.streakDays > 0 -> MotivationKind.STREAK to s.streakDays
        s.overdueCards > 0 -> MotivationKind.CATCH_UP to s.overdueCards
        s.matureCards > 0 -> MotivationKind.MATURE to s.matureCards
        else -> MotivationKind.KEEP to 0
    }
}
