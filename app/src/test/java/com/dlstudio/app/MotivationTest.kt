package com.dlstudio.app

import com.dlstudio.app.progress.DayPart
import com.dlstudio.app.progress.Mood
import com.dlstudio.app.progress.Motivation
import com.dlstudio.app.progress.Progress
import com.dlstudio.app.settings.AccentColor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MotivationTest {
    private val today = 20_000L

    @Test fun moodFollowsBehaviourOnly() {
        assertEquals(Mood.FIRST_STEP, Motivation.mood(Progress(), today))
        assertEquals(Mood.DONE_TODAY, Motivation.mood(Progress(xp = 10, streak = 3, lastDay = today), today))
        assertEquals(Mood.KEEP_STREAK, Motivation.mood(Progress(xp = 10, streak = 3, lastDay = today - 1), today))
        assertEquals(Mood.COMEBACK, Motivation.mood(Progress(xp = 10, streak = 3, lastDay = today - 5), today))
    }

    @Test fun dayPartCoversEveryHour() {
        assertEquals(DayPart.MORNING, Motivation.dayPart(7))
        assertEquals(DayPart.DAY, Motivation.dayPart(13))
        assertEquals(DayPart.EVENING, Motivation.dayPart(20))
        assertEquals(DayPart.NIGHT, Motivation.dayPart(2))
        assertEquals(DayPart.NIGHT, Motivation.dayPart(23))
    }

    @Test fun itemOfDayIsStableAndRotates() {
        val items = listOf("a", "b", "c")
        assertEquals(Motivation.ofDay(items, today), Motivation.ofDay(items, today))
        assertEquals(items.toSet(), (0L..2L).map { Motivation.ofDay(items, today + it) }.toSet())
        assertEquals("a", Motivation.ofDay(items, 0))
    }

    @Test fun whiteTextReadsOnEveryHue() {
        fun luminance(argb: Long): Double {
            fun lin(shift: Int): Double { val c = ((argb shr shift) and 0xFF) / 255.0; return if (c <= 0.04045) c / 12.92 else Math.pow((c + 0.055) / 1.055, 2.4) }
            return 0.2126 * lin(16) + 0.7152 * lin(8) + 0.0722 * lin(0)
        }
        for (hue in 0..360 step 5) {
            val accent = AccentColor(hue.toFloat())
            assertTrue("main at $hue", 1.05 / (luminance(accent.main) + 0.05) >= 2.9)
            assertTrue("deep at $hue", 1.05 / (luminance(accent.deep) + 0.05) >= 4.4)
        }
    }
}
