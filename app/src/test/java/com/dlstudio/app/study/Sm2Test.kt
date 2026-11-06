package com.dlstudio.app.study

import org.junit.Assert.assertEquals
import org.junit.Test

class Sm2Test {
    private val today = 100L

    @Test fun intervalsGrowOnePerSixThenEase() {
        val a = Sm2.next(null, Grade.KNOW, today)
        val b = Sm2.next(a, Grade.KNOW, today + 1)
        val c = Sm2.next(b, Grade.KNOW, today + 7)
        assertEquals(listOf(1, 6), listOf(a.intervalDays, b.intervalDays))
        assertEquals(17, c.intervalDays)
    }

    @Test fun againResetsAndStaysDueToday() {
        val learned = Sm2.next(Sm2.next(null, Grade.KNOW, 100), Grade.KNOW, 101)
        val lapsed = Sm2.next(learned, Grade.AGAIN, 107)
        assertEquals(1, lapsed.intervalDays)
        assertEquals(107L, lapsed.dueDay)
        assertEquals(1, lapsed.lapses)
        assertEquals(0, lapsed.reps)
    }

    @Test fun earlyReviewDoesNotInflateInterval() {
        var s = Sm2.next(null, Grade.KNOW, today)
        repeat(5) { s = Sm2.next(s, Grade.KNOW, today) }
        assertEquals(1, s.intervalDays)
        assertEquals(1, s.reps)
    }

    @Test fun easeStaysInBounds() {
        var s = Sm2.next(null, Grade.HARD, today)
        repeat(30) { s = Sm2.next(s, Grade.AGAIN, today) }
        assertEquals(Sm2.MIN_EASE, s.ease, 1e-9)
        repeat(30) { s = Sm2.next(s, Grade.KNOW, s.dueDay) }
        assertEquals(Sm2.MAX_EASE, s.ease, 1e-9)
    }

    @Test fun hardGrowsSlowerThanKnow() {
        val base = CardState(10, 2.5, 3, 0, today, today - 10)
        assertEquals(12, Sm2.next(base, Grade.HARD, today).intervalDays)
        assertEquals(26, Sm2.next(base, Grade.KNOW, today).intervalDays)
    }
}
