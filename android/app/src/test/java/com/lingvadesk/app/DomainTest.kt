package com.lingvadesk.app

import com.lingvadesk.app.domain.model.Card
import com.lingvadesk.app.domain.model.Grade
import com.lingvadesk.app.domain.model.Grade.*
import com.lingvadesk.app.domain.model.StudyCard
import com.lingvadesk.app.domain.srs.*
import com.lingvadesk.app.domain.study.*
import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

/** Порт JS-тестов: nextSrs, v230 (темп, разброс), behavior (оценка по поведению), speech, серии. */
class DomainTest {
    private val now = 1_767_225_600_000L
    private val fresh = SrsState(2.5, 0, 0, 0)
    private fun n(s: SrsState, g: Grade, pace: Double = 1.0, fuzz: Double = 0.0) = Srs.next(s, g, now, pace, fuzz)

    @Test fun againIsSoft() {
        val s = n(SrsState(2.5, 40, 5, now), AGAIN)
        assertEquals(0, s.reps); assertEquals(10, s.interval); assertEquals(now + 10 * Srs.MINUTE, s.due)
        assertEquals(2.4, s.ease, 1e-9)
        val second = Srs.next(s, AGAIN, now + 11 * Srs.MINUTE)
        assertEquals(10, second.interval); assertEquals(2.2, second.ease, 1e-9)
    }

    @Test fun relearning() {
        val lapsed = n(SrsState(2.5, 40, 5, now), AGAIN)
        assertEquals(10, n(lapsed, GOOD).interval)
        assertEquals(6, n(lapsed, HARD).interval)
        assertEquals(14, n(lapsed, EASY).interval)
        assertEquals(11, n(n(lapsed, GOOD), GOOD).interval)
    }

    @Test fun newCards() {
        val a = n(fresh, AGAIN)
        assertEquals(0, a.interval); assertEquals(0, a.reps); assertEquals(1, n(a, GOOD).interval)
        val g = n(fresh, GOOD)
        assertEquals(1, g.interval); assertEquals(6, n(g, GOOD).interval); assertEquals(now + Srs.DAY, g.due)
        assertEquals(4, n(fresh, EASY).interval)
        assertEquals(3, n(SrsState(2.5, 1, 1, 0), HARD).interval)
        assertEquals(8, n(SrsState(2.5, 1, 1, 0), EASY).interval)
        assertTrue(n(fresh, EASY).ease > 2.5); assertTrue(n(fresh, HARD).ease < 2.5)
    }

    @Test fun growthAndEaseFloor() {
        var s = fresh; var prev = 0
        repeat(5) { s = n(s, GOOD); assertTrue(s.interval > prev); prev = s.interval }
        s = fresh
        repeat(20) { s = n(s, AGAIN) }
        assertTrue(s.ease >= 1.3)
        repeat(20) { s = n(s.copy(reps = 3, interval = 5), HARD) }
        assertTrue(s.ease >= 1.3)
    }

    @Test fun paceAndFuzz() {
        val c = SrsState(2.5, 10, 4, 1)
        assertEquals(listOf(25, 19, 33), listOf(1.0, 0.75, 1.3).map { n(c, GOOD, it).interval })
        assertEquals(1, n(fresh, GOOD, 0.5).interval)
        assertEquals(n(c, AGAIN).interval, n(c, AGAIN, 1.7).interval)
        assertEquals(listOf(26, 24, 25), listOf(1.0, -1.0, 0.0).map { n(c, GOOD, 1.0, it).interval })
        val young = SrsState(2.5, 1, 2, 1)
        assertEquals(n(young, GOOD, 1.0, 1.0).interval, n(young, GOOD, 1.0, -1.0).interval)
    }

    private fun sig(think: Long? = null, look: Long = 0, dwell: Long = 0, flips: Int = 0, revisit: Boolean = false, away: Boolean = false) =
        Signals(think, look, dwell, flips, revisit, away)

    @Test fun behaviourGrades() {
        val i = GradeInference::infer
        assertNull(i(sig(dwell = 800), 0))
        assertNull(i(sig(600, 400, 1000, 1), 0))
        assertNull(i(sig(2000, 2000, 4000, 1, away = true), 0))
        assertEquals(GOOD, i(sig(dwell = 2500), 0)); assertEquals(EASY, i(sig(dwell = 2500), 3))
        assertEquals(GOOD, i(sig(dwell = 6000), 3)); assertEquals(HARD, i(sig(dwell = 10000), 3))
        fun g(think: Long, look: Long, dwell: Long, flips: Int = 1, revisit: Boolean = false) = i(sig(think, look, dwell, flips, revisit), 0)
        assertEquals(EASY, g(2000, 2000, 4000)); assertEquals(GOOD, g(1000, 1000, 2000))
        assertEquals(GOOD, g(3500, 2000, 5500)); assertEquals(GOOD, g(5000, 2000, 7000))
        assertEquals(HARD, g(5000, 6000, 11000)); assertEquals(HARD, g(10000, 2000, 12000))
        assertEquals(AGAIN, g(10000, 6000, 16000))
        assertEquals(GOOD, g(2000, 2000, 4000, flips = 4)); assertEquals(HARD, g(2000, 2000, 4000, 4, true))
        assertEquals(HARD, g(1000, 4000, 5000, revisit = true))
    }

    @Test fun speechGrades() {
        val sg = GradeInference::speechGrade
        assertEquals(EASY, sg(0.95, 1, false)); assertEquals(GOOD, sg(0.95, 2, false)); assertEquals(GOOD, sg(0.95, 1, true))
        assertEquals(HARD, sg(0.8, 1, true)); assertEquals(GOOD, sg(0.8, 1, false))
        assertEquals(HARD, sg(0.6, 1, false)); assertEquals(HARD, sg(0.6, 1, true)); assertEquals(AGAIN, sg(0.3, 1, false))
    }

    @Test fun speechScore() {
        fun s(t: String, target: String, alt: List<String> = emptyList(), c: Double = -1.0) = SpeechScore.score(t, alt, c, target)
        assertEquals(1.0, s("apple", "apple"), 0.0); assertEquals(1.0, s("Apple!", "apple"), 0.0)
        assertEquals(0.8, s("aple", "apple"), 0.0); assertEquals(1.0, s("aple", "apple", listOf("apple")), 0.0)
        assertEquals(0.94, s("apple", "apple", c = 0.6), 0.0); assertEquals(1.0, s("apple", "apple", c = 0.0), 0.0)
        assertEquals(0.0, s("", "apple"), 0.0); assertTrue(s("banana", "apple") < 0.3)
        assertEquals(1.0, s("where is the station", "Where is the station?"), 0.0)
        assertTrue(s("where the station", "Where is the station?") > 0.7)
    }

    @Test fun streaks() {
        val days = listOf(1L, 2, 3, 5, 6)           // 1–3 и 5–6 марта
        assertEquals(listOf(1, 2, 3), Streaks.compute(days, 6, 1).let { listOf(it.n, it.current, it.best) })
        assertEquals(listOf(0, 2, 3), Streaks.compute(days, 7, 0).let { listOf(it.n, it.current, it.best) })
        assertEquals(listOf(0, 0, 3), Streaks.compute(days, 9, 0).let { listOf(it.n, it.current, it.best) })
        assertEquals(0, Streaks.compute(emptyList(), 5, 0).best)
    }

    private fun sc(id: Long, deck: Long, ord: Int = 0, due: Long = 0, back: String = "b$id") =
        StudyCard(Card(id, deck, "f$id", back, ord = ord, due = due), "en-US")

    @Test fun planRespectsLimits() {
        val cards = (1L..5).map { sc(it, 1, it.toInt()) } + (6L..10).map { sc(it, 2, it.toInt()) }
        val limits = Limits(5, 99, emptyMap(), mapOf(1L to DeckLimits(perDay = 2), 2L to DeckLimits(max = 3)))
        val plan = SessionPlanner.plan(cards, now, limits)
        assertEquals(2, plan.count { it.card.deckId == 1L }); assertEquals(3, plan.count { it.card.deckId == 2L })
        val ordered = SessionPlanner.plan(cards, now, Limits(3, 99, emptyMap(), mapOf(1L to DeckLimits(inOrder = true))))
        assertEquals(listOf(1, 2, 3), ordered.filter { it.card.deckId == 1L }.map { it.card.ord })
        // использованные сегодня новые уменьшают остаток; просроченные — первыми, давние раньше
        val mixed = listOf(sc(1, 1, due = now - 10), sc(2, 1, due = now - 20), sc(3, 1, due = now + 1000), sc(4, 1))
        val p = SessionPlanner.plan(mixed, now, Limits(1, 99, mapOf(1L to 1)))
        assertEquals(listOf(2L, 1L), p.map { it.card.id })
    }

    @Test fun smallDeckHasNoChoice() {
        val pool = listOf(sc(1, 1), sc(2, 1))
        val kinds = (1..80).map { Quiz.pick(pool[0], pool, true, false, emptyMap(), Random(it)) }.toSet()
        assertEquals(setOf(TaskKind.FLIP, TaskKind.TYPE), kinds)
        assertNull(Quiz.make(pool[0], pool, false, TaskKind.CHOICE))
        val big = (1L..5).map { sc(it, 1) }
        assertEquals(4, Quiz.make(big[0], big, false, TaskKind.CHOICE)!!.choices.size)
        assertTrue(Quiz.isCorrect(QuizTask(TaskKind.TYPE, "x", "Яблоко; плод"), "плод"))
    }

    @Test fun deckPrefsIgnoreGarbage() {
        val st = mapOf("deck:1:newPerDay" to "3", "deck:1:pace" to "1.3", "deck:2:sessionMax" to "999", "deck:x:pace" to "2")
        val l = DeckPrefs.limitsFrom(st, listOf(1L, 2L), emptyMap())
        assertEquals(DeckLimits(3, null, false, 1.3), l.decks[1L]); assertEquals(DeckLimits(), l.decks[2L])
        assertEquals(10, l.perDay); assertEquals(50, l.max)
        assertEquals("theme", "theme"); assertEquals("auto", PrefOptions.next(PrefOptions.theme, "dark").value)
    }
}
