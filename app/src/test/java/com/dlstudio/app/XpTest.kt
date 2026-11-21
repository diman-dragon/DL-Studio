package com.dlstudio.app

import com.dlstudio.app.content.Card
import com.dlstudio.app.content.QuizItem
import com.dlstudio.app.content.TheoryPage
import com.dlstudio.app.content.Topic
import com.dlstudio.app.progress.Progress
import com.dlstudio.app.progress.Reward
import com.dlstudio.app.progress.TopicProgress
import com.dlstudio.app.progress.Xp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class XpTest {
    private val topic = Topic(
        "t", "Course", "A1", 1, "Title", "",
        listOf(TheoryPage("", listOf("p"))),
        listOf(Card("a", "b", "", ""), Card("c", "d", "", "")),
        listOf(QuizItem("q1", "x", listOf("x", "y")), QuizItem("q2", "x", listOf("x", "y")))
    )

    @Test fun theoryGivesXpOnlyOnce() {
        val first = Xp.theory(Progress(), topic, 10)
        assertEquals(Xp.THEORY, first.gained)
        assertEquals(0, Xp.theory(first.progress, topic, 10).gained)
    }

    @Test fun wrongAnswerGivesNothing() {
        assertEquals(0, Xp.answer(Progress(), topic, 0, false, 10).gained)
    }

    @Test fun finishingTheWholeTopicGivesBonusOnce() {
        var p = Progress()
        var total = 0
        fun take(r: Reward) { p = r.progress; total += r.gained }
        take(Xp.theory(p, topic, 5))
        take(Xp.card(p, topic, 0, 5))
        take(Xp.card(p, topic, 1, 5))
        take(Xp.answer(p, topic, 0, true, 5))
        val last = Xp.answer(p, topic, 1, true, 5)
        take(last)
        assertTrue(last.finished)
        assertEquals(Xp.THEORY + 2 * Xp.CARD + 2 * Xp.ANSWER + Xp.TOPIC, total)
        assertEquals(total, p.xp)
        assertEquals(0, Xp.answer(p, topic, 1, true, 5).gained)
    }

    @Test fun passMarkIsSixtyPercentRoundedUp() {
        assertEquals(1, Xp.passMark(1))
        assertEquals(2, Xp.passMark(2))
        assertEquals(3, Xp.passMark(4))
        assertEquals(3, Xp.passMark(5))
    }

    @Test fun stagesAreCounted() {
        val p = TopicProgress(theory = true, known = setOf(0, 1))
        assertEquals(2, Xp.stagesDone(topic, p))
        assertFalse(Xp.complete(topic, p))
    }

    @Test fun levelsComeEveryHundredXp() {
        assertEquals(1, Xp.level(99))
        assertEquals(2, Xp.level(100))
        assertEquals(40, Xp.intoLevel(240))
    }

    @Test fun streakGrowsDayByDayAndBreaks() {
        val day1 = Xp.theory(Progress(), topic, 100).progress
        assertEquals(1, day1.streak)
        val day2 = Xp.card(day1, topic, 0, 101).progress
        assertEquals(2, day2.streak)
        val sameDay = Xp.card(day2, topic, 1, 101).progress
        assertEquals(2, sameDay.streak)
        assertEquals(2, Xp.currentStreak(sameDay, 102))
        assertEquals(0, Xp.currentStreak(sameDay, 103))
        assertEquals(1, Xp.answer(sameDay, topic, 0, true, 110).progress.streak)
    }
}
