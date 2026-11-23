package com.dlstudio.app

import com.dlstudio.app.content.TopicJson
import com.dlstudio.app.content.TopicPack
import com.dlstudio.app.progress.Progress
import com.dlstudio.app.progress.ProgressJson
import com.dlstudio.app.progress.TopicProgress
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ContentTest {
    private val minimal = """{"id":"a-1","title":"T","cards":[{"front":"f","back":"b"}]}"""

    @Test fun minimalTopicParses() {
        val t = TopicJson.parse(minimal)
        assertEquals("a-1", t.id)
        assertEquals(1, t.cards.size)
        assertTrue(t.theory.isEmpty() && t.quiz.isEmpty())
    }

    @Test(expected = IllegalArgumentException::class) fun unsafeIdIsRejected() {
        TopicJson.parse("""{"id":"../x","title":"T","cards":[{"front":"f","back":"b"}]}""")
    }

    @Test(expected = IllegalArgumentException::class) fun emptyTopicIsRejected() {
        TopicJson.parse("""{"id":"x","title":"T"}""")
    }

    @Test fun quizWithoutRightOptionIsDropped() {
        val t = TopicJson.parse("""{"id":"x","title":"T","quiz":[{"q":"1","a":"yes","options":["no","maybe"]},{"q":"2","a":"yes","options":["yes","no"]}]}""")
        assertEquals(listOf("2"), t.quiz.map { it.question })
    }

    @Test fun progressSurvivesRoundTripAndGarbage() {
        val p = Progress(120, 3, 77, mapOf("a-1" to TopicProgress(true, setOf(0, 2), setOf(1), false)), bestStreak = 5, history = mapOf(76L to 20, 77L to 35))
        assertEquals(p, ProgressJson.decode(ProgressJson.encode(p)))
        assertEquals(Progress(), ProgressJson.decode("not json"))
        assertEquals(Progress(), ProgressJson.decode(null))
    }

    /** Старое сохранение (до 0.4.0) без лучшей серии и истории читается: лучшая серия не меньше текущей. */
    @Test fun legacyProgressWithoutBestStreakIsRead() {
        val old = """{"xp":120,"streak":3,"day":77,"topics":{"a-1":{"t":true,"k":[0,2],"q":[1],"b":false}}}"""
        val p = ProgressJson.decode(old)
        assertEquals(3, p.bestStreak)
        assertTrue(p.history.isEmpty())
        assertEquals(TopicProgress(true, setOf(0, 2), setOf(1), false), p.topics["a-1"])
    }

    private fun zip(vararg entries: Pair<String, String>): ByteArray {
        val out = ByteArrayOutputStream()
        ZipOutputStream(out).use { z -> entries.forEach { (name, text) -> z.putNextEntry(ZipEntry(name)); z.write(text.toByteArray()); z.closeEntry() } }
        return out.toByteArray()
    }

    @Test fun plainJsonIsOneTopicAndZipIsAPack() {
        assertEquals(listOf(minimal), TopicPack.texts(minimal.toByteArray()))
        val pack = zip("topics/a-1.json" to minimal, "topics/a-2.json" to minimal.replace("a-1", "a-2"), "readme.txt" to "не тема", "topics/" to "")
        assertEquals(listOf("a-1", "a-2"), TopicPack.texts(pack).map { TopicJson.parse(it).id })
    }

    @Test fun packSkipsOversizedEntriesAndSurvivesGarbage() {
        val big = "x".repeat(1_100_000)
        assertEquals(listOf(minimal), TopicPack.texts(zip("big.json" to big, "ok.json" to minimal)))
        assertTrue(TopicPack.texts("PK\u0003\u0004 это не архив".toByteArray()).isEmpty())
    }
}
