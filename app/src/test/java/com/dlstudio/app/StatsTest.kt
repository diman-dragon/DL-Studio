package com.dlstudio.app

import com.dlstudio.app.progress.Counts
import com.dlstudio.app.progress.Progress
import com.dlstudio.app.progress.ProgressJson
import com.dlstudio.app.progress.Stats
import com.dlstudio.app.speech.Grade
import org.junit.Assert.assertEquals
import org.junit.Test

class StatsTest {
    @Test fun answersAreCountedByKindTopicAndDay() {
        val s = Stats().answered("CHOICE", "t1", true, 100).answered("CHOICE", "t1", false, 100).answered("FILL", "t2", true, 101)
        assertEquals(Counts(2, 1), s.answers)
        assertEquals(Counts(1, 1), s.byKind.getValue("CHOICE"))
        assertEquals(Counts(1, 0), s.byTopic.getValue("t2"))
        assertEquals(Counts(1, 1), s.days.getValue(100).answers)
        assertEquals(66, s.answers.percent)
        assertEquals(0, Counts().percent)
    }

    @Test fun repeatsAreCountedAndOldDaysDropped() {
        val s = Stats().cardSeen(10).cardSeen(10).pageSeen().listened().listened().cardSeen(100)
        assertEquals(3, s.cards)
        assertEquals(1, s.pages)
        assertEquals(2, s.listens)
        assertEquals(listOf(0, 0, 1), s.lastDays(100, 3).map { it.cards })
        assertEquals(false, s.days.containsKey(10L))
    }

    @Test fun pronunciationKeepsAverageBestAndGrades() {
        val s = Stats().pronounced(90, Grade.GREAT, 5).pronounced(50, Grade.RETRY, 5)
        assertEquals(2, s.pron.tries)
        assertEquals(70, s.pron.average)
        assertEquals(90, s.pron.best)
        assertEquals(1, s.pron.great)
        assertEquals(1, s.pron.retry)
        assertEquals(2, s.days.getValue(5).tries)
    }

    @Test fun statsSurviveJsonRoundTripAndOldProgressWithoutThem() {
        val stats = Stats().answered("SPEAK", "t", false, 7).cardSeen(7).pageSeen().listened().pronounced(80, Grade.GOOD, 7)
        val back = ProgressJson.decode(ProgressJson.encode(Progress(xp = 5, stats = stats)))
        assertEquals(stats, back.stats)
        assertEquals(5, back.xp)
        assertEquals(Stats(), ProgressJson.decode("{\"xp\":3,\"streak\":1,\"day\":1,\"topics\":{}}").stats)
    }
}
