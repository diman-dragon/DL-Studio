package com.dlstudio.app.study

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SlowSpeechTest {
    @Test fun singleWordIsOnePartWithoutPause() {
        assertEquals(listOf(SlowSpeech.Part("cat", 0)), SlowSpeech.parts("cat"))
    }

    @Test fun phraseWithCommasAndWordsIsNeverChopped() {
        val text = "Well, I would like a cup of coffee please"
        assertEquals(listOf(SlowSpeech.Part(text, 0)), SlowSpeech.parts(text))
    }

    @Test fun onlySentenceEndsGetPauses() {
        val parts = SlowSpeech.parts("Well, I know. Really? Yes!")
        assertEquals(listOf("Well, I know.", "Really?", "Yes!"), parts.map { it.text })
        assertTrue(parts[0].pauseMs > 0 && parts[1].pauseMs > 0)
        assertEquals(0, parts.last().pauseMs)
    }

    @Test fun pauseScaleStretchesPauses() {
        val normal = SlowSpeech.parts("One. Two.")
        val longer = SlowSpeech.parts("One. Two.", 2f)
        assertEquals(normal[0].pauseMs * 2, longer[0].pauseMs)
    }

    @Test fun zeroPauseReadsWholeText() {
        assertEquals(listOf(SlowSpeech.Part("One. Two.", 0)), SlowSpeech.parts("One. Two.", 0f))
    }

    @Test fun sampleMatchesVoiceLanguage() {
        assertTrue(SlowSpeech.sample("ru-RU").any { it in 'а'..'я' })
        assertTrue(SlowSpeech.sample(null).none { it in 'а'..'я' })
    }
}
