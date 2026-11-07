package com.dlstudio.app.study

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
