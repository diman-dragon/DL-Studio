package com.dlstudio.app.library

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LibraryEditTest {
    private val deck = listOf("p", "d")
    private val tree = listOf(
        LibNode("p", "English B1", children = listOf(LibNode("d", "Unit 1", children = listOf(LibNode("c", "cat", "кот")))))
    )

    @Test fun addsCardInsideDeck() {
        val result = tree.addAt(deck, LibNode("c2", "dog", "пёс"))
        assertEquals(listOf("c", "c2"), result.childrenAt(deck).map { it.id })
    }

    @Test fun addsProjectAtTop() {
        assertEquals(listOf("p", "n"), tree.addAt(emptyList(), LibNode("n", "German A2")).map { it.id })
    }

    @Test fun editsCard() {
        val result = tree.editAt(deck + "c") { it.copy(name = "cats") }
        assertEquals("cats", result.findAt(deck + "c")?.name)
        assertEquals("кот", result.findAt(deck + "c")?.back)
    }

    @Test fun removesDeckWithCards() {
        val result = tree.editAt(deck) { null }
        assertTrue(result.childrenAt(listOf("p")).isEmpty())
        assertNull(result.findAt(deck))
    }

    @Test fun missingPathIsEmpty() {
        assertNull(tree.findAt(listOf("x")))
        assertTrue(tree.childrenAt(listOf("x")).isEmpty())
    }
}
