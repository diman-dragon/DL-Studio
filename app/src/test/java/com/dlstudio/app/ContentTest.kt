package com.dlstudio.app

import com.dlstudio.app.content.Remote
import com.dlstudio.app.content.TopicJson
import com.dlstudio.app.progress.Progress
import com.dlstudio.app.progress.ProgressJson
import com.dlstudio.app.progress.TopicProgress
import java.io.File
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

    @Test fun catalogSkipsUnsafePaths() {
        val text = """{"topics":[{"id":"ok","pack":"topics/ok.json","sha256":"00"},{"id":"bad","pack":"../secret.json","sha256":"00"},{"id":"bad2","pack":"https://evil/x.json","sha256":"00"}]}"""
        assertEquals(listOf("ok"), TopicJson.parseCatalog(text).map { it.id })
    }

    @Test fun sha256MatchesKnownValue() {
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", Remote.sha256("abc".toByteArray()))
    }

    @Test fun progressSurvivesRoundTripAndGarbage() {
        val p = Progress(120, 3, 77, mapOf("a-1" to TopicProgress(true, setOf(0, 2), setOf(1), false)))
        assertEquals(p, ProgressJson.decode(ProgressJson.encode(p)))
        assertEquals(Progress(), ProgressJson.decode("not json"))
        assertEquals(Progress(), ProgressJson.decode(null))
    }

    /** Встроенные темы проекта должны читаться, а каталог — совпадать с ними по числу и контрольным суммам. */
    @Test fun shippedTopicsAreValid() {
        val files = File("../content/topics").listFiles { f -> f.extension == "json" }.orEmpty().sortedBy { it.name }
        assertTrue(files.isNotEmpty())
        val catalog = TopicJson.parseCatalog(File("../content/course.json").readText()).associateBy { it.id }
        files.forEach { f ->
            val t = TopicJson.parse(f.readText())
            assertEquals(f.nameWithoutExtension, t.id)
            assertEquals("course.json устарел: запустите scripts/build_content.mjs для ${t.id}", catalog[t.id]?.sha256, Remote.sha256(f.readBytes()))
        }
        assertEquals(files.size, catalog.size)
    }
}
