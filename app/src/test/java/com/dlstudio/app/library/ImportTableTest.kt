package com.dlstudio.app.library

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ImportTableTest {
    private fun cards(text: String) = ImportTable.decks(text, "файл").flatMap { it.children }.map { it.name to it.back }

    @Test fun semicolonPairsGoToOneDeckNamedAfterFile() {
        val decks = ImportTable.decks("cat;кот\ndog;собака\n", "words")
        assertEquals(listOf("words"), decks.map { it.name })
        assertEquals(listOf("cat" to "кот", "dog" to "собака"), cards("cat;кот\ndog;собака\n"))
    }

    @Test fun tabsDashesAndCommasAreDetected() {
        assertEquals(listOf("a" to "б"), cards("a\tб"))
        assertEquals(listOf("cat" to "кот, кошка"), cards("cat - кот, кошка"))
        assertEquals(listOf("hello" to "привет"), cards("hello, привет"))
    }

    @Test fun headerSetsColumnsAndSplitsIntoDecks() {
        val decks = ImportTable.decks("перевод;слово;колода\nкот;cat;Animals\nдом;house;Home\nпёс;dog;Animals", "x")
        assertEquals(listOf("Animals", "Home"), decks.map { it.name })
        assertEquals(listOf("cat" to "кот", "dog" to "пёс"), decks[0].children.map { it.name to it.back })
    }

    @Test fun wordThatLooksLikeHeaderStaysACard() {
        assertEquals(listOf("back" to "спина"), cards("back;спина"))
    }

    @Test fun ankiDirectivesQuotesAndSingleColumnRows() {
        val text = "#separator:tab\n#html:false\n\"to run, to jog\"\t\"бежать\"\nfoo\nbar\tбар\n"
        assertEquals(listOf("to run, to jog" to "бежать", "bar" to "бар"), cards(text))
    }

    @Test fun nothingUsefulGivesNoDecks() {
        assertTrue(ImportTable.decks("просто текст без разделителей", "x").isEmpty())
        assertTrue(ImportTable.decks("", "x").isEmpty())
    }
}
