package com.dlstudio.app.study

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SlowSpeechTest {
    @Test fun singleWordHasNoPause() {
        assertEquals(listOf(SlowSpeech.Part("cat", 0)), SlowSpeech.parts("cat"))
    }

    @Test fun pausesAfterPunctuationAreLongerAfterSentenceEnd() {
        val parts = SlowSpeech.parts("Well, I know. Really?")
        assertEquals(listOf("Well,", "I know.", "Really?"), parts.map { it.text })
        assertTrue(parts[1].pauseMs > parts[0].pauseMs)
        assertEquals(0, parts.last().pauseMs)
    }

    @Test fun longPhraseIsSplitIntoShortGroups() {
        val parts = SlowSpeech.parts("I would like a cup of coffee please")
        assertTrue(parts.size >= 2)
        assertTrue(parts.all { it.text.split(" ").size <= 4 })
        assertEquals("I would like a cup of coffee please", parts.joinToString(" ") { it.text })
    }

    @Test fun textWithoutLettersIsKeptAsIs() {
        assertEquals(listOf(SlowSpeech.Part("...", 0)), SlowSpeech.parts("..."))
    }

    @Test fun pauseScaleStretchesPauses() {
        val normal = SlowSpeech.parts("Well, I know. Really?")
        val longer = SlowSpeech.parts("Well, I know. Really?", 2f)
        assertEquals(normal[0].pauseMs * 2, longer[0].pauseMs)
    }

    @Test fun zeroPauseReadsWholePhrase() {
        assertEquals(listOf(SlowSpeech.Part("Well, I know. Really?", 0)), SlowSpeech.parts("Well, I know. Really?", 0f))
    }

    @Test fun sampleMatchesVoiceLanguage() {
        assertTrue(SlowSpeech.sample("ru-RU").any { it in 'а'..'я' })
        assertTrue(SlowSpeech.sample(null).none { it in 'а'..'я' })
    }
}
