package com.dlstudio.app

import com.dlstudio.app.i18n.StringsEn
import com.dlstudio.app.i18n.StringsRu
import com.dlstudio.app.progress.GoalPhase
import com.dlstudio.app.progress.Achievements
import com.dlstudio.app.progress.Badge
import com.dlstudio.app.progress.Progress
import com.dlstudio.app.progress.ProgressJson
import com.dlstudio.app.progress.Totals
import com.dlstudio.app.progress.Week
import com.dlstudio.app.progress.rankIndex
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InsightsTest {
    // 2026-11-22 — воскресенье; 1970-01-01 (день 0) — четверг.
    private val sunday = 20_779L

    @Test fun weekStartsOnMonday() {
        assertEquals(6, Week.weekday(sunday))
        assertEquals(sunday - 6, Week.monday(sunday))
        assertEquals(0, Week.weekday(Week.monday(sunday)))
        assertEquals(0, Week.weekday(0 + 4))
    }

    @Test fun weekCollectsXpByDay() {
        val p = Progress(history = mapOf(sunday to 12, sunday - 6 to 30, sunday - 7 to 99))
        assertEquals(listOf(30, 0, 0, 0, 0, 0, 12), Week.xpByDay(p, sunday))
        assertEquals(12, Week.todayXp(p, sunday))
    }

    @Test fun goalIsCrossedOnlyOnce() {
        val before = Progress(history = mapOf(sunday to 25))
        val after = Progress(history = mapOf(sunday to 32))
        assertTrue(Week.crossedGoal(before, after, sunday, 30))
        assertFalse(Week.crossedGoal(after, Progress(history = mapOf(sunday to 40)), sunday, 30))
    }

    @Test fun badgesComeFromTotals() {
        val none = Totals(0, 0, 0, 0, 0)
        assertFalse(Achievements.unlocked(Badge.FIRST_XP, none))
        assertEquals(Badge.FIRST_XP, Achievements.nextUp(Totals(5, 0, 0, 0, 0)))
        assertTrue(Achievements.unlocked(Badge.STREAK_7, Totals(0, 7, 0, 0, 0)))
        assertNull(Achievements.nextUp(Totals(10_000, 100, 100, 1000, 1000)))
    }

    @Test fun everyBadgeHasTextInBothLanguages() {
        for (s in listOf(StringsRu, StringsEn)) {
            assertEquals(Badge.values().size, s.badgeNames.size)
            assertEquals(Badge.values().size, s.badgeHints.size)
            assertEquals(7, s.weekdays.size)
            assertTrue(s.cheers.isNotEmpty())
            GoalPhase.values().forEach { assertTrue(s.goalNudge(it, 12).isNotBlank()) }
        }
    }

    @Test fun ranksNeverRunOut() {
        assertEquals(0, rankIndex(1, 10))
        assertEquals(9, rankIndex(500, 10))
    }

    @Test fun historySurvivesSaving() {
        val p = Progress(xp = 50, streak = 2, lastDay = sunday, bestStreak = 5, history = mapOf(sunday to 20, sunday - 1 to 30))
        val back = ProgressJson.decode(ProgressJson.encode(p))
        assertEquals(p.history, back.history)
        assertEquals(5, back.bestStreak)
    }
}
