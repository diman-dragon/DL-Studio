package com.dlstudio.app

import com.dlstudio.app.content.Answers
import com.dlstudio.app.content.Block
import com.dlstudio.app.content.Media
import com.dlstudio.app.content.MediaKind
import com.dlstudio.app.content.QuizItem
import com.dlstudio.app.content.QuizKind
import com.dlstudio.app.content.TheoryBlocks
import com.dlstudio.app.content.Topic
import com.dlstudio.app.content.TheoryPage
import com.dlstudio.app.speech.Cand
import com.dlstudio.app.speech.Gender
import com.dlstudio.app.speech.LangNeed
import com.dlstudio.app.speech.Needs
import com.dlstudio.app.speech.PackState
import com.dlstudio.app.speech.RawVoice
import com.dlstudio.app.speech.Role
import com.dlstudio.app.speech.VoiceCast
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

    private fun roles(vararg lines: String) = TheoryBlocks.parse(lines.toList()).map { (it as Block.Say).role }

    @Test fun neighbourPhrasesGetDifferentVoices() {
        assertEquals(listOf(0), roles("~ Hi").map { it.n })
        assertEquals(listOf(0, 1), roles("~ Hi", "~ Hello").map { it.n })
        assertEquals(listOf(0, 1, 2, 3), roles("~ a", "~ b", "~ c", "~ d").map { it.n })
        assertEquals(listOf(0, 0, 0, 0, 0), roles("~ a", "~ b", "~ c", "~ d", "~ e").map { it.n })
    }

    @Test fun marksSetGenderAndNumber() {
        assertEquals(listOf(Role(Gender.F, 0), Role(Gender.M, 0), Role(Gender.F, 1), Role(Gender.ANY, 2)), roles("~f Hello | Привет", "~m Hi", "~f2 Bye", "~3 Yo"))
        assertEquals("Hello", (TheoryBlocks.parse(listOf("~f Hello | Привет"))[0] as Block.Say).text)
        assertTrue(TheoryBlocks.parse(listOf("~for you"))[0] is Block.Text)
        assertEquals(setOf(Gender.F, Gender.M), TheoryBlocks.genders(listOf("~f a", "~m b", "text", "~ c")))
        assertTrue(TheoryBlocks.genders(listOf("~ a", "~2 b")).isEmpty())
    }

    @Test fun genderIsReadFromVoiceNames() {
        assertEquals(Gender.F, Gender.of("en-us-x-sfg#female_1-local"))
        assertEquals(Gender.M, Gender.of("en-us-x-abc#male_2-local"))
        assertEquals(Gender.ANY, Gender.of("en-us-x-iom-local"))
        assertEquals(Gender.M, Gender.of("x", setOf("male")))
    }

    private fun cand(name: String, gender: Int, quality: Int = 400) = Cand(name, gender, false, true, quality)

    @Test fun maleRoleNeverGetsFemaleVoice() {
        val all = listOf(cand("f1", Gender.F), cand("m1", Gender.M))
        assertEquals("m1", VoiceCast.pick(all, Role(Gender.M), "f1", emptyMap()).voice)
        assertEquals("f1", VoiceCast.pick(all, Role(Gender.F), "m1", emptyMap()).voice)
        val onlyFemale = listOf(cand("f1", Gender.F), cand("f2", Gender.F))
        val cast = VoiceCast.pick(onlyFemale, Role(Gender.M), "f1", emptyMap())
        assertEquals("f1", cast.voice)
        assertTrue(cast.pitch < 1f)
    }

    @Test fun assignedVoiceCountsAsThatGenderAndSecondHeroGetsAnotherVoice() {
        val unknown = listOf(cand("a", Gender.ANY), cand("b", Gender.M, 500))
        assertEquals("a", VoiceCast.pick(unknown, Role(Gender.M), null, mapOf(Gender.M to "a")).voice)
        val men = listOf(cand("m1", Gender.M), cand("m2", Gender.M))
        assertEquals(listOf("m1", "m2", "m1"), (0..2).map { VoiceCast.pick(men, Role(Gender.M, it), null, emptyMap()).voice })
        assertTrue(VoiceCast.pick(men, Role(Gender.M, 2), null, emptyMap()).pitch < 1f)
    }

    @Test fun roleWithoutGenderUsesOtherVoicesOppositeGenderFirst() {
        val all = listOf(cand("f1", Gender.F), cand("f2", Gender.F), cand("m1", Gender.M))
        assertEquals("f1", VoiceCast.pick(all, Role(), "f1", emptyMap()).voice)
        assertEquals("m1", VoiceCast.pick(all, Role(Gender.ANY, 1), "f1", emptyMap()).voice)
        assertEquals("f2", VoiceCast.pick(all, Role(Gender.ANY, 2), "f1", emptyMap()).voice)
    }

    @Test fun needsFindLanguageAndMissingPieces() {
        val topic = Topic("t", "c", "g", 1, "T", "en-US", listOf(TheoryPage("", listOf("~f Hi", "~m Yo"))), emptyList(), emptyList())
        val voices = listOf(RawVoice("v#female_1", "en-US", 400, false, true, Gender.F), RawVoice("v#male_1", "en-US", 400, false, false, Gender.M))
        val need = Needs.of(listOf(topic), voices, mapOf("en-US" to PackState.AVAILABLE), emptyMap()).single()
        assertEquals("en", need.lang)
        assertEquals(false, need.lackFemale)
        assertTrue(need.lackMale)
        assertEquals(listOf("en-US"), need.packs)
        assertEquals(emptyList<LangNeed>(), Needs.of(listOf(topic.copy(tag = "")), voices, emptyMap(), emptyMap()))
    }

    @Test fun voiceNamesFollowGender() {
        val raw = listOf(RawVoice("a#female_1", "en-US", 400, false, true, Gender.F), RawVoice("b", "en-US", 400, false, true), RawVoice("c", "en-US", 400, false, true))
        val named = VoiceNames.choices(raw, "en", false, mapOf("en:m" to "c")).associateBy { it.name }
        assertEquals(Gender.F, named.getValue("a#female_1").gender)
        assertEquals(Gender.ANY, named.getValue("b").gender)
        assertEquals(Gender.M, named.getValue("c").gender)
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
