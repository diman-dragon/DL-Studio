package com.dlstudio.app.library

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ImportParserTest {
    @Test fun tableBecomesProjectWithDeck() {
        val project = ImportParser.parse("cat;кот", "Unit 1").single()
        assertEquals("Unit 1", project.name)
        assertEquals("Unit 1", project.children.single().name)
        assertEquals("кот", project.children.single().children.single().back)
    }

    @Test fun jsonTreeKeepsStructureAndGetsNewIds() {
        val json = """[{"id":"same","name":"B1","items":[{"id":"same","name":"U1","items":[{"front":"cat","translation":"кот"}]}]}]"""
        val project = ImportParser.parse(json, "f").single()
        assertEquals("B1", project.name)
        assertEquals("cat", project.children.single().children.single().name)
        assertEquals("кот", project.children.single().children.single().back)
        assertNotEquals("same", project.id)
        assertNotEquals(project.id, project.children.single().id)
    }

    @Test fun flatJsonCardsAreWrappedIntoProjectAndDeck() {
        val project = ImportParser.parse("""[{"name":"cat","back":"кот"},{"name":"dog","back":"пёс"}]""", "pets").single()
        assertEquals(listOf("cat", "dog"), project.children.single().children.map { it.name })
    }

    @Test fun brokenJsonFallsBackToTable() {
        val project = ImportParser.parse("[noun] cat;кот", "f").single()
        assertEquals("[noun] cat", project.children.single().children.single().name)
    }

    @Test fun descendAdaptsToOpenLevel() {
        val tree = ImportParser.parse("a;1\nb;2", "f")
        assertEquals(listOf("f"), tree.descend(0).map { it.name })
        assertEquals(listOf("f"), tree.descend(1).map { it.name })
        assertEquals(listOf("a", "b"), tree.descend(2).map { it.name })
        assertTrue(tree.descend(3).isEmpty())
    }

    @Test fun addAllAtAppendsInsideDeck() {
        val library = listOf(LibNode("p", "P", children = listOf(LibNode("d", "D"))))
        val result = library.addAllAt(listOf("p", "d"), listOf(LibNode("c1", "x"), LibNode("c2", "y")))
        assertEquals(listOf("c1", "c2"), result.childrenAt(listOf("p", "d")).map { it.id })
    }
}
