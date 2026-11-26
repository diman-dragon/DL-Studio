package com.dlstudio.app

import com.dlstudio.app.content.Answers
import com.dlstudio.app.content.CourseCast
import com.dlstudio.app.content.QuizItem
import com.dlstudio.app.content.QuizKind
import com.dlstudio.app.content.TheoryPage
import com.dlstudio.app.content.Topic
import com.dlstudio.app.content.TopicJson
import com.dlstudio.app.speech.Cand
import com.dlstudio.app.speech.CharRole
import com.dlstudio.app.speech.Characters
import com.dlstudio.app.speech.Gender
import com.dlstudio.app.speech.How
import com.dlstudio.app.speech.RawVoice
import com.dlstudio.app.speech.VoiceCast
import com.dlstudio.app.content.Card
import com.dlstudio.app.progress.Place
import com.dlstudio.app.progress.PlaceJson
import com.dlstudio.app.progress.TopicProgress
import com.dlstudio.app.progress.Xp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CastTest {
    private fun cand(name: String, gender: Int, quality: Int = 400, network: Boolean = false) = Cand(name, gender, network, true, quality)
    private fun cands(f: Int, m: Int) = (1..f).map { cand("f$it", Gender.F, 500 - it) } + (1..m).map { cand("m$it", Gender.M, 500 - it) }
    private fun assign(c: List<Cand>, chosen: Map<String, String> = emptyMap(), sticky: Map<String, String> = emptyMap()) = VoiceCast.assign(c, chosen, sticky)

    @Test fun charactersHaveRolesGendersAndAreUnique() {
        assertEquals(listOf("grace", "leo", "beatrice", "john", "narrator"), Characters.all.map { it.id })
        assertEquals(listOf(CharRole.TEACHER, CharRole.TEACHER, CharRole.STUDENT, CharRole.STUDENT, CharRole.NARRATOR), Characters.all.map { it.role })
        assertEquals(listOf(Gender.F, Gender.M, Gender.F, Gender.M, Gender.ANY), Characters.all.map { it.gender })
        assertEquals(5, Characters.all.map { it.tint }.toSet().size)
        assertEquals("grace", Characters.of(null).id)
        assertEquals("grace", Characters.of("nobody").id)
        assertEquals("john", Characters.of("john").id)
        assertEquals(listOf("grace", "leo"), Characters.teachers.map { it.id })
    }

    @Test fun everyCharacterSoundsDifferentInEveryVoiceSetup() {
        for ((f, m) in listOf(0 to 0, 1 to 1, 1 to 0, 0 to 1, 2 to 2, 3 to 3, 6 to 1, 1 to 6)) {
            val voices = cands(f, m)
            val plan = assign(voices)
            assertEquals("$f/$m", 5, plan.size)
            assertEquals("$f/$m", 5, plan.values.map { Triple(it.voice, it.pitch, it.rate) }.toSet().size)
            for (who in Characters.all.filter { it.gender != Gender.ANY }) plan.getValue(who.id).voice?.let { v ->
                assertEquals("$f/$m ${who.id} got $v", who.gender, voices.first { it.name == v }.gender)
            }
        }
    }

    @Test fun enoughVoicesGiveEveryoneAnOwnVoice() {
        val plan = assign(cands(3, 3))
        assertEquals(5, plan.values.map { it.voice }.toSet().size)
        assertTrue(plan.values.all { it.how == How.OWN })
        assertEquals("f1", plan.getValue("grace").voice)
        assertEquals("m1", plan.getValue("leo").voice)
        assertEquals("f2", plan.getValue("beatrice").voice)
        assertEquals("m2", plan.getValue("john").voice)
    }

    @Test fun chosenVoiceWinsAndOthersStayDistinct() {
        val plan = assign(cands(2, 2), chosen = mapOf("beatrice" to "f1"))
        assertEquals("f1", plan.getValue("beatrice").voice)
        assertEquals(How.CHOSEN, plan.getValue("beatrice").how)
        assertEquals("f2", plan.getValue("grace").voice)
        assertEquals("m1", plan.getValue("leo").voice)
        assertNotEquals(plan.getValue("grace").voice, plan.getValue("beatrice").voice)
    }

    @Test fun chosenVoiceOfWrongGenderOrMissingVoiceIsIgnored() {
        val plan = assign(cands(2, 2), chosen = mapOf("leo" to "f1", "grace" to "gone"))
        assertEquals("m1", plan.getValue("leo").voice)
        assertEquals("f1", plan.getValue("grace").voice)
    }

    @Test fun assignedVoiceStaysWhenBetterVoiceAppears() {
        val before = listOf(cand("f1", Gender.F, 300), cand("m1", Gender.M))
        val first = assign(before)
        val sticky = first.filterValues { it.how == How.OWN && it.voice != null }.mapValues { it.value.voice!! }
        assertEquals("f1", first.getValue("grace").voice)
        val after = before + cand("f2", Gender.F, 500)
        assertEquals("f2", assign(after).getValue("grace").voice)
        val kept = assign(after, sticky = sticky)
        assertEquals("f1", kept.getValue("grace").voice)
        assertEquals("f2", kept.getValue("beatrice").voice)
    }

    @Test fun staleStickyVoiceIsReplaced() {
        val plan = assign(cands(2, 1), sticky = mapOf("grace" to "removed", "leo" to "f1"))
        assertEquals("f1", plan.getValue("grace").voice)
        assertEquals("m1", plan.getValue("leo").voice)
    }

    @Test fun maleCharactersNeverGetFemaleVoice() {
        val plan = assign(cands(2, 0))
        for (id in listOf("leo", "john")) {
            assertNull(plan.getValue(id).voice)
            assertEquals(How.DEFAULT, plan.getValue(id).how)
            assertTrue(plan.getValue(id).pitch < 1f)
        }
        assertTrue(plan.getValue("john").pitch != plan.getValue("leo").pitch)
    }

    @Test fun withoutAnyVoiceCharactersDifferByPitchAndPace() {
        val plan = assign(emptyList())
        assertTrue(plan.values.all { it.voice == null && it.how == How.DEFAULT })
        assertTrue(plan.getValue("beatrice").pitch > plan.getValue("grace").pitch)
        assertTrue(plan.getValue("john").pitch < plan.getValue("leo").pitch)
    }

    @Test fun sharedVoiceIsSeparatedByPitchAndPace() {
        val plan = assign(cands(1, 1))
        assertEquals(plan.getValue("grace").voice, plan.getValue("beatrice").voice)
        assertEquals(How.SHARED, plan.getValue("beatrice").how)
        assertTrue(plan.getValue("beatrice").pitch > plan.getValue("grace").pitch)
        assertEquals(plan.getValue("leo").voice, plan.getValue("john").voice)
        assertTrue(plan.getValue("john").pitch < plan.getValue("leo").pitch)
    }

    @Test fun planUsesOnlyInstalledVoicesOfTopicLanguage() {
        val raw = listOf(
            RawVoice("en-a#female_1", "en-US", 400, false, true, Gender.F), RawVoice("en-b#male_1", "en-US", 400, false, true, Gender.M),
            RawVoice("en-c#female_2", "en-GB", 400, false, false, Gender.F), RawVoice("ru-a#female_1", "ru-RU", 500, false, true, Gender.F)
        )
        val plan = VoiceCast.plan(raw, "en-US", mapOf("en:beatrice" to "en-a#female_1", "ru:grace" to "ru-a#female_1"), emptyMap())
        assertEquals("en-a#female_1", plan.getValue("beatrice").voice)
        assertEquals(How.CHOSEN, plan.getValue("beatrice").how)
        assertNotEquals("ru-a#female_1", plan.getValue("grace").voice)
        assertEquals("en-b#male_1", plan.getValue("leo").voice)
    }

    private fun page(vararg lines: String) = TheoryPage("", lines.toList())

    @Test fun pageSpeakersFollowMarksAndUnmarkedIsGrace() {
        assertEquals(listOf("grace", "john"), CourseCast.onPage(page("~ Hi", "text", "~john Hello", "~ Bye", "~john Ok")))
        assertEquals(emptyList<String>(), CourseCast.onPage(page("plain text", "- a")))
        assertEquals(listOf("john", "beatrice"), CourseCast.fresh(page("~john a", "~beatrice b", "~grace c"), setOf("grace")))
        assertEquals(emptyList<String>(), CourseCast.fresh(page("~john a"), setOf("john")))
    }

    private fun topic(id: String, theory: List<TheoryPage>, quiz: List<QuizItem> = emptyList()) = Topic(id, "C", "G", 1, id, "en-US", theory, emptyList(), quiz)

    @Test fun courseCastCollectsEveryoneWhoSpeaks() {
        val a = topic("a", listOf(page("~beatrice Hi", "~john Hello")))
        val b = topic("b", listOf(page("~ Welcome")), listOf(QuizItem("q", "x", listOf("x", "y"), QuizKind.LISTEN, "x", who = "leo"), QuizItem("q2", "x", listOf("x", "y"), who = "narrator")))
        assertEquals(listOf("grace", "leo", "beatrice", "john"), CourseCast.of(listOf(a, b)).map { it.id })
        assertEquals(emptyList<String>(), CourseCast.of(listOf(topic("c", listOf(page("plain"))))).map { it.id })
    }

    @Test fun cardFieldsAreReadAndMissingOnesStayEmpty() {
        val cards = TopicJson.parse("""{"id":"t","title":"T","tag":"en-US","cards":[
            {"front":"go","back":"идти","pos":"глагол","ipa":"/ɡoʊ/","forms":"go — went — gone","example":"I go.","exampleTr":"Я иду.","note":"Объяснение"},
            {"front":"cat","back":"кот"}]}""").cards
        assertEquals(listOf("глагол", "/ɡoʊ/", "go — went — gone", "I go.", "Я иду.", "Объяснение"), with(cards[0]) { listOf(pos, ipa, forms, example, exampleTr, note) })
        assertEquals(listOf("", "", "", "", "", ""), with(cards[1]) { listOf(pos, ipa, forms, example, exampleTr, note) })
    }

    @Test fun choiceOptionsAreShuffledSoAnswerMovesAround() {
        val options = listOf("a", "b", "c", "d")
        val places = (1..300).map { Answers.order(options, kotlin.random.Random(it)).also { o -> assertEquals(options, o.sorted()) }.indexOf("a") }.toSet()
        assertEquals(setOf(0, 1, 2, 3), places)
    }

    @Test fun quizWhoIsReadFromJsonAndUnknownIsDropped() {
        fun quiz(who: String) = TopicJson.parse("""{"id":"t","title":"T","tag":"en-US","quiz":[{"t":"listen","say":"Hi","a":"Hi","options":["Hi","Yo"],"who":"$who"}]}""").quiz.single().who
        assertEquals("john", quiz("john"))
        assertEquals("john", quiz("JOHN"))
        assertNull(quiz("bob"))
        assertNull(quiz(""))
    }

    private fun match(n: Int, q: String = "m") = QuizItem(q, "", emptyList(), QuizKind.MATCH, pairs = List(n) { "l$it" to "r$it" })

    @Test fun matchColumnsAreBothShuffledAndStable() {
        for (n in 3..7) for (q in listOf("a", "b", "Соедини", "x$n")) {
            val (left, right) = Answers.matchOrder(match(n, q))
            val plain = List(n) { it }
            assertEquals(plain, left.sorted())
            assertEquals(plain, right.sorted())
            assertNotEquals(plain, left)
            assertNotEquals(plain, right)
            assertNotEquals(left, right)
            assertEquals(left to right, Answers.matchOrder(match(n, q)))
        }
        assertEquals(listOf(1, 0), Answers.matchOrder(match(2)).first)
        assertEquals(listOf(0) to listOf(0), Answers.matchOrder(match(1)))
    }

    @Test fun needsSoundCoversListenSpeakAndDictation() {
        fun item(kind: QuizKind, say: String = "", q: String = "q") = QuizItem(q, "a", listOf("a", "b"), kind, say = say)
        assertTrue(item(QuizKind.LISTEN, "Hi").needsSound)
        assertTrue(item(QuizKind.SPEAK, "Hi").needsSound)
        assertTrue(item(QuizKind.FILL, "Hi", "").needsSound)
        assertTrue(!item(QuizKind.FILL, "Hi", "Fill the gap").needsSound)
        assertTrue(!item(QuizKind.CHOICE).needsSound)
        assertTrue(!item(QuizKind.MATCH).needsSound)
    }

    @Test fun resumePointsToFirstUnfinished() {
        val topic = Topic("t", "", "", 1, "T", "", List(3) { TheoryPage("h$it", emptyList()) }, List(4) { Card("f$it", "b$it", "", "") }, emptyList())
        assertEquals(2, Xp.startCard(topic, TopicProgress(seen = setOf(0, 1, 3))))
        assertEquals(0, Xp.startCard(topic, TopicProgress(seen = setOf(0, 1, 2, 3))))
        assertEquals(1, Xp.startQuestion(listOf(0, 2, 3), TopicProgress(quizOk = setOf(0, 3))))
        assertEquals(0, Xp.startQuestion(listOf(0, 2), TopicProgress(quizOk = setOf(0, 2))))
        val here = Place(1, 2, 340, 3, 5, 120)
        assertEquals(mapOf("t" to here), PlaceJson.decode(PlaceJson.encode(mapOf("t" to here))))
        assertEquals(emptyMap<String, Place>(), PlaceJson.decode("{oops"))
        assertEquals(emptyMap<String, Place>(), PlaceJson.decode(null))
    }

    @Test fun gapAndFindQuestions() {
        val gap = QuizItem("Io ___ a Roma e tu ___ a Mosca.", "abito | abiti", listOf("abita", "abitano"), QuizKind.GAP)
        assertEquals(listOf("Io ", " a Roma e tu ", " a Mosca."), Answers.gapParts(gap))
        assertEquals(listOf("abito", "abiti"), Answers.gapWords(gap.answer))
        assertEquals(listOf("abito", "abiti"), Answers.gapWords("abito abiti"))
        assertEquals(setOf("abito", "abiti", "abita", "abitano"), Answers.gapBag(gap).toSet())
        assertEquals(Answers.gapBag(gap), Answers.gapBag(gap))
        assertTrue(Answers.gapOk(gap, listOf("Abito", "abiti")))
        assertTrue(!Answers.gapOk(gap, listOf("abiti", "abito")))
        assertEquals("Io abito a Roma e tu abiti a Mosca.", Answers.gapFilled(gap))
        val find = QuizItem("Carlo e Nicola colleziona francobolli.", "colleziona", emptyList(), QuizKind.FIND, hint = "Правильно: collezionano")
        assertEquals(3, Answers.findIndex(find))
        assertEquals(-1, Answers.findIndex(find.copy(answer = "nulla")))
    }
}
