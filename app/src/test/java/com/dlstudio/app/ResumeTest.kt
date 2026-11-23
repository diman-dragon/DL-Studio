package com.dlstudio.app

import com.dlstudio.app.content.Card
import com.dlstudio.app.content.Topic
import com.dlstudio.app.progress.GoalPhase
import com.dlstudio.app.progress.Motivation
import com.dlstudio.app.progress.Resume
import com.dlstudio.app.progress.TopicProgress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ResumeTest {
    private fun topic(id: String, group: String, n: Int, course: String = "C") =
        Topic(id, course, group, n, id, "", emptyList(), listOf(Card("a", "b", "", "")), emptyList())

    private val a = topic("a", "L1", 1)
    private val b = topic("b", "L1", 2)
    private val c = topic("c", "L2", 3)
    private val d = topic("d", "L2", 4)
    private val all = listOf(a, b, c, d)
    private fun done(vararg t: Topic) = t.associate { it.id to TopicProgress(seen = setOf(0)) }

    @Test fun unfinishedLastTopicIsWhereYouContinue() {
        assertEquals(c, Resume.next(all, done(a), "c"))
        assertEquals(c, Resume.focus(all, done(a), "c"))
    }

    @Test fun finishedLastTopicMovesOnToTheNextOpenOne() {
        assertEquals(c, Resume.next(all, done(a, b), "b"))
        assertEquals(b, Resume.next(all, done(a, c, d), "d"))
    }

    @Test fun withoutHistoryItIsTheFirstOpenTopicAndMissingLastIsIgnored() {
        assertEquals(a, Resume.next(all, emptyMap(), ""))
        assertEquals(b, Resume.next(all, done(a), "gone"))
        assertNull(Resume.last(all, "gone"))
        assertNull(Resume.next(all, done(a, b, c, d), "d"))
    }

    @Test fun focusStaysOnTheLastTopicEvenWhenFinished() {
        assertEquals(b, Resume.focus(all, done(a, b), "b"))
        assertEquals(a, Resume.focus(all, emptyMap(), ""))
    }

    @Test fun lessonsKeepOrderAndActiveFollowsFocus() {
        val lessons = Resume.lessons(all)
        assertEquals(listOf("C/L1", "C/L2"), lessons.map { it.key })
        assertEquals("C/L2", Resume.active(lessons, d)?.key)
        assertEquals("C/L1", Resume.active(lessons, null)?.key)
    }

    @Test fun otherCourseDoesNotStealFocus() {
        val other = topic("x", "L1", 1, course = "D")
        assertEquals("C", Resume.focus(all + other, emptyMap(), "d")?.course)
        assertEquals(other, Resume.next(listOf(other), emptyMap(), "d"))
    }

    @Test fun goalPhaseFollowsProgress() {
        assertEquals(GoalPhase.START, Motivation.goalPhase(0, 30))
        assertEquals(GoalPhase.EARLY, Motivation.goalPhase(5, 30))
        assertEquals(GoalPhase.HALF, Motivation.goalPhase(15, 30))
        assertEquals(GoalPhase.CLOSE, Motivation.goalPhase(25, 30))
        assertEquals(GoalPhase.DONE, Motivation.goalPhase(30, 30))
    }
}
