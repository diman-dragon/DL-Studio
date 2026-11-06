package com.dlstudio.app.study

import com.dlstudio.app.library.LibNode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.TimeZone

class StudyLogicTest {
    private val cards = (1..6).map { LibNode("c$it", "w$it") }
    private fun st(due: Long) = CardState(6, 2.5, 2, 0, due, due - 6)

    @Test fun pickerOrdersDueThenNewAndLimits() {
        val states = mapOf("c1" to st(10), "c2" to st(8), "c3" to st(50))
        assertEquals(listOf("c2", "c1", "c4", "c5"), SessionPicker.pick(cards, states, 10, 4).map { it.id })
    }

    @Test fun pickerReachesCardsBeyondLimitOnLaterDays() {
        val states = cards.take(2).associate { it.id to st(5) }
        assertEquals(listOf("c1", "c2", "c3"), SessionPicker.pick(cards, states, 5, 3).map { it.id })
    }

    @Test fun pickerExtraOnlyWhenNothingPlanned() {
        val states = cards.associate { it.id to st(50) }
        assertTrue(SessionPicker.pick(cards, states, 10, 3).isEmpty())
        assertEquals(3, SessionPicker.pick(cards, states, 10, 3, extra = true).size)
    }

    @Test fun sessionRequeuesAgainAndCountsOnce() {
        var s = StudySession.start(cards.take(2), 0)
        assertNull(s.answer(Grade.KNOW, 5))
        s = s.flip(1000)
        val (n, rec) = s.answer(Grade.AGAIN, 3000)!!
        assertEquals(1000L, rec.faceMs); assertEquals(2000L, rec.backMs)
        assertEquals(3, n.queue.size); assertEquals(1, n.again)
        var cur = n
        repeat(1) { cur = cur.flip(0).answer(Grade.KNOW, 0)!!.first }
        val last = cur.flip(0).answer(Grade.HARD, 0)!!.first
        assertTrue(last.done); assertEquals(2, last.finished)
    }

    @Test fun dayNumberFloorsBeforeEpochAndKeepsTimezone() {
        val utc = TimeZone.getTimeZone("UTC")
        assertEquals(-1L, DayMath.dayNumber(-1, utc))
        assertEquals(0L, DayMath.dayNumber(0, utc))
        assertEquals(1L, DayMath.dayNumber(DayMath.DAY - 1, TimeZone.getTimeZone("GMT+1")))
        assertEquals(3, DayMath.weekday(0))
    }

    @Test fun streakCountsFromYesterdayWhenNotStudiedToday() {
        assertEquals(3, Streak.count(setOf(7L, 8, 9), 10))
        assertEquals(4, Streak.count(setOf(7L, 8, 9, 10), 10))
        assertEquals(0, Streak.count(setOf(7L, 8), 10))
    }

    @Test fun statsUseDayBoundaries() {
        val states = listOf(st(9), st(10), st(11), CardState(40, 2.5, 5, 0, 60, 20))
        val per = mapOf(10L to DayAgg(4, 8000, 3), 9L to DayAgg(1, 1000, 1))
        val s = StatsCalc.compute(6, states, per, setOf(9L, 10L), 10)
        assertEquals(2, s.dueCards); assertEquals(1, s.overdueCards); assertEquals(2, s.newCards)
        assertEquals(4, s.todayReviews); assertEquals(5, s.reviewsByDay[12] + s.reviewsByDay[13])
        assertEquals(80, s.accuracy); assertEquals(2, s.streakDays); assertEquals(1, s.matureCards)
        assertEquals(listOf(0, 3, 0, 1), s.intervalBuckets)
    }

    @Test fun countsSeparateNewDueOverdue() {
        val c = CardCounts.of(cards, mapOf("c1" to st(3), "c2" to st(10)), 10)
        assertEquals(listOf(6, 4, 2, 1), listOf(c.total, c.fresh, c.due, c.overdue))
    }

    @Test fun reminderRoundTripAndRules() {
        val snap = ReminderSnapshot.of(2, 9, listOf(st(9), st(12)))
        val back = ReminderSnapshot.decode(snap.encode())!!
        assertEquals(snap, back)
        assertTrue(back.shouldNotify(10)); assertTrue(!ReminderSnapshot(0, 10, mapOf(12L to 1)).shouldNotify(10))
        assertTrue(ReminderSnapshot(5, 9, emptyMap()).shouldNotify(10))
        assertTrue(!ReminderSnapshot(5, 10, emptyMap()).shouldNotify(10))
        assertNull(ReminderSnapshot.decode("junk"))
    }

    @Test fun scopeSelectsProjectDeckOrAll() {
        val tree = listOf(LibNode("p1", "a", children = listOf(LibNode("d1", "x", children = cards.take(2)), LibNode("d2", "y", children = cards.drop(4)))), LibNode("p2", "b", children = listOf(LibNode("d3", "z", children = cards.subList(2, 4)))))
        assertEquals(6, StudyScope.cards(tree, null, null).size)
        assertEquals(4, StudyScope.cards(tree, "p1", null).size)
        assertEquals(2, StudyScope.cards(tree, "p1", "d2").size)
    }
}
