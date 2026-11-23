package com.dlstudio.app

import com.dlstudio.app.content.Answers
import com.dlstudio.app.content.Block
import com.dlstudio.app.content.Media
import com.dlstudio.app.content.MediaKind
import com.dlstudio.app.content.QuizItem
import com.dlstudio.app.content.QuizKind
import com.dlstudio.app.content.TheoryBlocks
import com.dlstudio.app.speech.RawVoice
import com.dlstudio.app.speech.VoiceNames
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LessonLogicTest {
    private fun fill(a: String, alt: List<String> = emptyList()) = QuizItem("q", a, emptyList(), QuizKind.FILL, alt = alt)

    @Test fun fillIgnoresCaseAndPunctuation() {
        assertTrue(Answers.fillOk(fill("I'm sorry", listOf("sorry")), "  i'M Sorry! "))
        assertTrue(Answers.fillOk(fill("sorry", listOf("I am sorry")), "i am sorry"))
        assertEquals(false, Answers.fillOk(fill("dog"), ""))
    }

    @Test fun bankChecksOrderAndNeverStartsSolved() {
        val item = QuizItem("q", "air arm arrow", emptyList(), QuizKind.BANK)
        assertTrue(Answers.bankOk(item, listOf("air", "arm", "arrow")))
        assertEquals(false, Answers.bankOk(item, listOf("arm", "air", "arrow")))
        assertTrue(Answers.bag(item) != listOf("air", "arm", "arrow"))
        assertEquals(listOf("air", "arm", "arrow"), Answers.bag(item).sorted())
    }

    @Test fun matchAllowsOneMistakeOnlyWithFourPairs() {
        assertTrue(Answers.matchOk(4, 1))
        assertEquals(false, Answers.matchOk(3, 1))
    }

    @Test fun theoryBlocksAreGrouped() {
        val b = TheoryBlocks.parse(listOf("text", "- a", "- b", "| h1 | h2", "| 1 | 2", "::v A a | эй | /eɪ/", "::c B b | би", "!tip hi", "~ Hi | Привет | hello"))
        assertEquals(listOf("text", "list", "table", "tiles", "tip", "say"), b.map { kind(it) })
        assertEquals(2, (b[1] as Block.Bullets).items.size)
        assertEquals("hello", (b[5] as Block.Say).say)
    }

    private fun kind(b: Block) = when (b) {
        is Block.Text -> "text"; is Block.Bullets -> "list"; is Block.Table -> "table"; is Block.Tiles -> "tiles"
        is Block.Callout -> "tip"; is Block.Say -> "say"; is Block.Sub -> "sub"; is Block.Media -> "media"
    }

    @Test fun voiceNamesAreHumanAndUnique() {
        val raw = (1..15).map { RawVoice("en-us-x-v$it#female_1-local", "en-US", 400, false, true) }
        val names = VoiceNames.choices(raw, "en", true).map { it.title }
        assertEquals(15, names.toSet().size)
        assertTrue(names.all { it.first().isLetter() })
    }

    @Test fun mediaBlocksAndSafePaths() {
        val b = TheoryBlocks.parse(listOf("@audio hello.mp3 | Диалог", "@video https://x.org/a.mp4", "@image"))
        assertEquals(MediaKind.AUDIO, (b[0] as Block.Media).kind)
        assertEquals("Диалог", (b[0] as Block.Media).caption)
        assertEquals("https://x.org/a.mp4", (b[1] as Block.Media).src)
        assertTrue(b[2] is Block.Text)
        assertTrue(Media.safePath("img/map.png"))
        assertEquals(false, Media.safePath("../secret.png"))
        assertEquals(false, Media.safePath("run.exe"))
    }
}
