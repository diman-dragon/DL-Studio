package com.dlstudio.app.study

import org.junit.Assert.assertEquals
import org.junit.Test

class PraiseTest {
    private val base = StudyStats(totalCards = 9, studiedCards = 5, reviewsByDay = List(14) { 0 })

    @Test fun recordDayBeatsEverything() {
        val days = List(13) { 3 } + 8
        assertEquals(PraiseKind.RECORD_DAY to 8, Praise.pick(base.copy(reviewsByDay = days, todayReviews = 8, yesterdayReviews = 3)))
    }

    @Test fun moreThanYesterday() {
        val days = List(12) { 9 } + listOf(2, 4)
        assertEquals(PraiseKind.UP_TODAY to 2, Praise.pick(base.copy(reviewsByDay = days, todayReviews = 4, yesterdayReviews = 2)))
    }

    @Test fun streakRecordNeedsThreeDays() {
        assertEquals(PraiseKind.BEST_STREAK to 5, Praise.pick(base.copy(streakDays = 5, bestStreak = 5)))
        assertEquals(PraiseKind.KEEP, Praise.pick(base.copy(streakDays = 2, bestStreak = 2)).first)
    }

    @Test fun weekGrowthInPercent() {
        assertEquals(PraiseKind.WEEK_UP to 50, Praise.pick(base.copy(weekReviews = 30, prevWeekReviews = 20, streakDays = 1, bestStreak = 9)))
    }

    @Test fun goodAccuracyAndFallback() {
        assertEquals(PraiseKind.ACCURACY to 90, Praise.pick(base.copy(weekReviews = 5, accuracy = 90, bestStreak = 9)))
        assertEquals(PraiseKind.KEEP to 0, Praise.pick(base))
    }

    @Test fun sessionPraiseLevels() {
        assertEquals(2, SessionPraise.level(8, 10))
        assertEquals(1, SessionPraise.level(5, 10))
        assertEquals(0, SessionPraise.level(1, 10))
        assertEquals(0, SessionPraise.level(0, 0))
    }

    @Test fun longestStreakAndWeekSums() {
        assertEquals(3, Streak.longest(setOf(1L, 2L, 3L, 7L, 8L)))
        assertEquals(0, Streak.longest(emptySet()))
        val s = StatsCalc.compute(3, emptyList(), (3L..10L).associateWith { DayAgg(2, 0, 2) }, (3L..10L).toSet(), 10)
        assertEquals(8, s.bestStreak)
        assertEquals(14, s.weekReviews)
        assertEquals(2, s.prevWeekReviews)
    }
}
