package com.dlstudio.app.study

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PronounceCheckTest {
    @Test fun caseAndPunctuationDoNotMatter() {
        assertEquals(PronounceVerdict.OK, PronounceCheck.verdict("That sounds good.", listOf("that sounds good")))
        assertEquals(PronounceVerdict.OK, PronounceCheck.verdict("I tend to think that…", listOf("I tend to think that")))
    }

    @Test fun leadingToIsOptional() {
        assertEquals(PronounceVerdict.OK, PronounceCheck.verdict("to suggest", listOf("suggest")))
        assertEquals(PronounceVerdict.OK, PronounceCheck.verdict("to suggest", listOf("to suggest")))
    }

    @Test fun bestOfAlternativesWins() {
        assertEquals(PronounceVerdict.OK, PronounceCheck.verdict("evidence", listOf("evidenced", "evidence", "a video")))
    }

    @Test fun smallSlipIsCloseAndOtherWordIsMiss() {
        assertEquals(PronounceVerdict.CLOSE, PronounceCheck.verdict("get something off your chest", listOf("get something of your")))
        assertEquals(PronounceVerdict.MISS, PronounceCheck.verdict("evidence", listOf("banana")))
        assertEquals(PronounceVerdict.MISS, PronounceCheck.verdict("evidence", emptyList()))
    }

    @Test fun similarityIsBetweenZeroAndOne() {
        assertEquals(1.0, PronounceCheck.similarity("cat", "Cat!"), 0.0)
        assertTrue(PronounceCheck.similarity("cat", "dog") < PronounceCheck.CLOSE_AT)
    }

    @Test fun languageFromVoiceOrText() {
        assertEquals("en-US", SpeechLang.fromVoice("en-us-x-tpf-local"))
        assertEquals(null, SpeechLang.fromVoice("Alyona"))
        assertEquals("ru-RU", SpeechLang.forText("привет", null))
        assertEquals("en-US", SpeechLang.forText("hello", null))
        assertEquals("de-DE", SpeechLang.forText("hallo", "de-DE"))
    }
}
