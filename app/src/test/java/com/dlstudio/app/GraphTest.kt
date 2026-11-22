package com.dlstudio.app

import com.dlstudio.app.content.Card
import com.dlstudio.app.content.TopicJson
import com.dlstudio.app.content.Topic
import com.dlstudio.app.progress.TopicGraph
import com.dlstudio.app.progress.TopicProgress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GraphTest {
    private fun topic(id: String, vararg requires: String) =
        Topic(id, "C", "G", 1, id, "", emptyList(), listOf(Card("a", "b", "", "")), emptyList(), requires.toList())

    private val a = topic("a")
    private val b = topic("b", "a")
    private val c = topic("c", "a")
    private val d = topic("d", "b", "c")

    @Test fun layersFollowTheLongestPrerequisiteChain() {
        assertEquals(mapOf("a" to 0, "b" to 1, "c" to 1, "d" to 2), TopicGraph.layers(listOf(a, b, c, d)))
    }

    @Test fun cyclesAndUnknownIdsDoNotHang() {
        val x = topic("x", "y", "ghost")
        val y = topic("y", "x")
        assertEquals(setOf("x", "y"), TopicGraph.layers(listOf(x, y)).keys)
    }

    @Test fun courseWithoutPrerequisitesIsATrail() {
        assertFalse(TopicGraph.isGraph(listOf(a, topic("z"))))
        assertFalse(TopicGraph.isGraph(listOf(topic("q", "missing"))))
        assertTrue(TopicGraph.isGraph(listOf(a, b)))
    }

    @Test fun topicOpensWhenPrerequisitesAreDone() {
        assertEquals(listOf("a"), TopicGraph.open(listOf(a, b, c, d), emptyMap()).map { it.id })
        val done = mapOf("a" to TopicProgress(true, setOf(0), emptySet(), true))
        assertEquals(listOf("b", "c"), TopicGraph.open(listOf(a, b, c, d), done).map { it.id })
    }

    @Test fun requiresAreReadFromJson() {
        val t = TopicJson.parse("""{"id":"m","title":"T","cards":[{"front":"f","back":"b"}],"requires":["a","../bad"]}""")
        assertEquals(listOf("a"), t.requires)
    }
}
