package com.dlstudio.app

import com.dlstudio.app.progress.Progress
import com.dlstudio.app.speech.Grade
import com.dlstudio.app.speech.Pronunciation
import com.dlstudio.app.progress.ProgressJson
import org.junit.Assert.assertEquals
import org.junit.Test

class PronunciationTest {
    @Test fun ignoresCaseAndPunctuation() {
        assertEquals("i am a student", Pronunciation.normalize("  I am, a Student! "))
        assertEquals(100, Pronunciation.score("I am a student.", listOf("i am a student")))
    }

    @Test fun takesTheBestRecognitionVariant() {
        assertEquals(100, Pronunciation.score("good morning", listOf("good mourning", "good morning")))
    }

    @Test fun differentPhraseScoresLow() {
        assertEquals(Grade.RETRY, Pronunciation.grade(Pronunciation.score("good morning", listOf("thank you very much"))))
    }

    @Test fun emptyInputGivesZero() {
        assertEquals(0, Pronunciation.score("", listOf("hello")))
        assertEquals(0, Pronunciation.score("hello", emptyList()))
    }

    @Test fun gradeThresholds() {
        assertEquals(Grade.GREAT, Pronunciation.grade(90))
        assertEquals(Grade.GOOD, Pronunciation.grade(70))
        assertEquals(Grade.RETRY, Pronunciation.grade(30))
    }

    @Test fun resetKeepsXpOnly() {
        val kept = Progress(xp = 120)
        assertEquals(120, ProgressJson.decode(ProgressJson.encode(kept)).xp)
        assertEquals(emptyMap<String, Any>(), ProgressJson.decode(ProgressJson.encode(kept)).topics)
    }
}
