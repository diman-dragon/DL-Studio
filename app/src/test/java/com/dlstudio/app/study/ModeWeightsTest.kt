package com.dlstudio.app.study

import com.dlstudio.app.library.LibNode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ModeWeightsTest {
    private val cards = (1..200).map { LibNode("id$it", "word$it", "слово$it") }

    @Test fun onlyEnabledModesAreUsed() {
        val enabled = setOf(CardMode.CHOICE_FWD, CardMode.WRITE_REV)
        val used = cards.mapIndexed { i, c -> ModeWeights.modeFor(c, i, enabled) }.toSet()
        assertEquals(enabled, used)
    }

    @Test fun noListeningNoTyping() {
        val enabled = setOf(CardMode.CHOICE_FWD, CardMode.CHOICE_REV)
        val used = cards.mapIndexed { i, c -> ModeWeights.modeFor(c, i, enabled) }.toSet()
        assertFalse(CardMode.LISTEN in used || CardMode.WRITE_FWD in used || CardMode.WRITE_REV in used)
    }

    @Test fun singleModeIsAlwaysThatMode() {
        assertTrue(cards.mapIndexed { i, c -> ModeWeights.modeFor(c, i, setOf(CardMode.LISTEN)) }.all { it == CardMode.LISTEN })
    }

    @Test fun lastModeCannotBeSwitchedOff() {
        val one = StudyOptions(modes = setOf(CardMode.LISTEN))
        assertEquals(one, one.withMode(CardMode.LISTEN, false))
        assertTrue(one.isLastMode(CardMode.LISTEN))
        assertEquals(2, one.withMode(CardMode.WRITE_FWD, true).modes.size)
    }
}
