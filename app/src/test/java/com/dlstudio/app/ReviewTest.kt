package com.dlstudio.app

import com.dlstudio.app.content.QuizItem
import com.dlstudio.app.content.QuizKind
import com.dlstudio.app.content.Topic
import com.dlstudio.app.data.CrashReport
import com.dlstudio.app.progress.Backup
import com.dlstudio.app.progress.Place
import com.dlstudio.app.progress.Progress
import com.dlstudio.app.progress.ProgressJson
import com.dlstudio.app.progress.Review
import com.dlstudio.app.progress.TopicProgress
import com.dlstudio.app.progress.Xp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReviewTest {
    private fun quiz(kind: QuizKind, say: String = "") = QuizItem("q", "a", listOf("a", "b"), kind, say = say)
    private val topic = Topic("t", "c", "g", 1, "Тема", "it", emptyList(), emptyList(), listOf(quiz(QuizKind.CHOICE), quiz(QuizKind.CHOICE), quiz(QuizKind.LISTEN, "Ciao"), quiz(QuizKind.SPEAK, "Ciao")))

    @Test fun wrongAnswersAreTrackedAndCleared() {
        val p0 = Progress()
        val wrong = Xp.answer(p0, topic, 1, false, 10)
        assertEquals(0, wrong.gained)
        assertEquals(setOf(1), wrong.progress.topics.getValue("t").miss)
        assertEquals(emptySet<Int>(), wrong.progress.topics.getValue("t").quizOk)
        val fixed = Xp.answer(wrong.progress, topic, 1, true, 10)
        assertTrue(fixed.gained > 0)
        assertEquals(emptySet<Int>(), fixed.progress.topics.getValue("t").miss)
        assertEquals(setOf(1), fixed.progress.topics.getValue("t").quizOk)
        assertEquals(wrong.progress.topics, ProgressJson.decode(ProgressJson.encode(wrong.progress)).topics)
    }

    @Test fun reviewCountsMistakesAndSkippedSound() {
        assertTrue(Review.items(listOf(topic), emptyMap()).isEmpty())
        val untouched = mapOf("t" to TopicProgress(theory = true))
        assertTrue(Review.items(listOf(topic), untouched).isEmpty())
        val started = mapOf("t" to TopicProgress(quizOk = setOf(0), miss = setOf(1)))
        val item = Review.items(listOf(topic), started).single()
        assertEquals(1, item.wrong)
        assertEquals(2, item.sound)
        val noVoice = Review.items(listOf(topic.copy(tag = "")), started).single()
        assertEquals(0, noVoice.sound)
    }

    @Test fun backupRoundTripAndRejectsForeignFiles() {
        val progress = Progress(xp = 120, topics = mapOf("t" to TopicProgress(quizOk = setOf(0), miss = setOf(2))))
        val places = mapOf("t" to Place(1, 2, 30, 3, 4, 50))
        val back = Backup.decode(Backup.encode(progress, places))
        assertEquals(progress.topics, back?.progress?.topics)
        assertEquals(120, back?.progress?.xp)
        assertEquals(places, back?.places)
        assertNull(Backup.decode("{\"hello\":1}"))
        assertNull(Backup.decode("not json"))
        assertNull(Backup.decode(null))
    }

    @Test fun crashReportHasContextAndCause() {
        val error = IllegalStateException("outer", ArithmeticException("inner"))
        val text = CrashReport.build("2026-11-26 10:00:00", "0.4.23", "Pixel, Android 14", "main", error)
        assertTrue(text.startsWith("DL Studio 0.4.23 · Pixel, Android 14"))
        assertTrue("IllegalStateException: outer" in text && "Caused by: java.lang.ArithmeticException: inner" in text)
        assertTrue(text.length <= CrashReport.LIMIT)
    }
}
