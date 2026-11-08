package com.dlstudio.app.study

import com.dlstudio.app.library.LibNode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ModeWeightsTest {
    private val cards = (1..2000).map { LibNode("id$it", "word$it", "слово$it") }
    private fun used(options: StudyOptions, list: List<LibNode> = cards) = list.mapIndexed { i, c -> ModeWeights.modeFor(c, i, options) }

    @Test fun onlyEnabledModesAreUsed() {
        val enabled = setOf(CardMode.CHOICE_FWD, CardMode.WRITE_REV)
        assertEquals(enabled, used(StudyOptions(modes = enabled)).toSet())
    }

    @Test fun noListeningNoTyping() {
        val u = used(StudyOptions(modes = setOf(CardMode.CHOICE_FWD, CardMode.CHOICE_REV))).toSet()
        assertFalse(CardMode.LISTEN in u || CardMode.WRITE_FWD in u || CardMode.WRITE_REV in u)
    }

    @Test fun singleModeIsAlwaysThatMode() {
        assertTrue(used(StudyOptions(modes = setOf(CardMode.LISTEN))).all { it == CardMode.LISTEN })
    }

    private val CardMode.frequent get() = this == CardMode.CHOICE_FWD || this == CardMode.CHOICE_REV

    @Test fun choiceCardsTakeAboutHalfByDefault() {
        val modes = used(StudyOptions())
        assertEquals(0.5, modes.count { it.frequent } / modes.size.toDouble(), 0.05)
        assertEquals(0.5, modes.count { it == CardMode.CHOICE_FWD } / modes.count { it.frequent }.toDouble(), 0.07)
    }

    @Test fun shareIsAdjustable() {
        assertEquals(0.9, used(StudyOptions(flipShare = 90)).count { it.frequent } / cards.size.toDouble(), 0.04)
        assertEquals(0.3, used(StudyOptions(flipShare = 30)).count { it.frequent } / cards.size.toDouble(), 0.04)
    }

    @Test fun reviewNeverUsesPlainFlip() {
        val u = used(StudyOptions()).toSet()
        assertEquals(CardMode.studied, u)
        assertFalse(CardMode.FLIP_FWD in u)
    }

    @Test fun withoutChoiceModesNoChoice() {
        assertTrue(used(StudyOptions(modes = CardMode.studied - CardMode.CHOICE_FWD - CardMode.CHOICE_REV)).none { it.frequent })
    }

    @Test fun pronounceOnlyForOneOrTwoWords() {
        val long = LibNode("x", "to give up on it", "сдаться")
        val verb = LibNode("y", "to go", "идти")
        val noBack = LibNode("z", "cat", "")
        assertFalse(ModeWeights.eligible(long, CardMode.PRONOUNCE))
        assertTrue(ModeWeights.eligible(verb, CardMode.PRONOUNCE))
        assertFalse(ModeWeights.eligible(noBack, CardMode.PRONOUNCE))
        val only = StudyOptions(modes = setOf(CardMode.PRONOUNCE))
        assertTrue(used(only, listOf(long, long)).all { it == CardMode.CHOICE_FWD })
        assertTrue(used(only, listOf(verb, verb)).all { it == CardMode.PRONOUNCE })
    }

    @Test fun modeIsStableForSameCardAndPage() {
        assertEquals(used(StudyOptions()), used(StudyOptions()))
    }

    @Test fun lastModeCannotBeSwitchedOff() {
        val one = StudyOptions(modes = setOf(CardMode.LISTEN))
        assertEquals(one, one.withMode(CardMode.LISTEN, false))
        assertTrue(one.isLastMode(CardMode.LISTEN))
        assertEquals(2, one.withMode(CardMode.WRITE_FWD, true).modes.size)
    }

    @Test fun repeatUsesSentenceOrWord() {
        val card = LibNode("r", "cat", "кот", example = "The cat sleeps")
        val targets = (0..40).map { SpeakTarget.of(card, CardMode.REPEAT, it) }.toSet()
        assertEquals(setOf("cat", "The cat sleeps"), targets)
        assertEquals("cat", SpeakTarget.of(card, CardMode.PRONOUNCE, 3))
        assertEquals("cat", SpeakTarget.of(LibNode("q", "cat", "кот"), CardMode.REPEAT, 1))
    }

    @Test fun speechGradeFollowsVerdict() {
        assertEquals(Grade.KNOW, AutoGrade.ofSpeech(PronounceVerdict.OK, false))
        assertEquals(Grade.HARD, AutoGrade.ofSpeech(PronounceVerdict.OK, true))
        assertEquals(Grade.HARD, AutoGrade.ofSpeech(PronounceVerdict.CLOSE, false))
        assertEquals(Grade.AGAIN, AutoGrade.ofSpeech(PronounceVerdict.MISS, false))
        assertEquals(Grade.AGAIN, AutoGrade.ofSpeech(null, false))
    }

    @Test fun matchedWordsAreMarkedPerWord() {
        assertEquals(listOf(true, false), PronounceCheck.matchedWords("Good morning", "good"))
        assertEquals(listOf(true, true), PronounceCheck.matchedWords("Good morning!", "Good Morning"))
        assertEquals(listOf(false), PronounceCheck.matchedWords("cat", ""))
    }
}
