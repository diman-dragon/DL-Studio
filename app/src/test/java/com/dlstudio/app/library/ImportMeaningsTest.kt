package com.dlstudio.app.library

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ImportMeaningsTest {
    private val json = """[{"name":"B1","items":[{"name":"U1","items":[
        {"name":"bank","ipa":"bæŋk","hint":"noun — a place where money is kept","meanings":[
            {"translation":"банк","explanation":"учреждение, где хранят деньги","example":"I went to the bank.","exampleTranslation":"Я ходил в банк."},
            {"translation":"берег","explanation":"край реки","example":"We sat on the bank.","exampleTranslation":"Мы сидели на берегу."},
            {"translation":"","explanation":"","example":"","exampleTranslation":""}]},
        {"name":"cat","back":"кот"}]}]}]"""

    private fun cards() = ImportParser.parse(json, "f").single().children.single().children

    @Test fun jsonKeepsIpaAndMeanings() {
        val bank = cards().first()
        assertEquals("bæŋk", bank.ipa)
        assertEquals(listOf("банк", "берег"), bank.meanings.map { it.translation })
        assertEquals("Я ходил в банк.", bank.meanings.first().exampleTranslation)
    }

    @Test fun mainTranslationComesFromFirstMeaning() { assertEquals("банк", cards().first().back) }

    @Test fun oldCardsHaveNoMeanings() {
        val cat = cards().last()
        assertEquals("кот", cat.back)
        assertEquals("", cat.ipa)
        assertTrue(cat.meanings.isEmpty())
    }

    @Test fun meaningsSurviveStorageRoundTrip() {
        val meanings = cards().first().meanings
        assertEquals(meanings, MeaningsJson.decode(MeaningsJson.encode(meanings)))
        assertEquals("", MeaningsJson.encode(emptyList()))
        assertTrue(MeaningsJson.decode("").isEmpty())
        assertTrue(MeaningsJson.decode("не json").isEmpty())
    }

    @Test fun tableReadsIpaColumn() {
        val card = ImportParser.parse("word;ipa;translation\ncat;kæt;кот", "f").single().children.single().children.single()
        assertEquals(Triple("cat", "kæt", "кот"), Triple(card.name, card.ipa, card.back))
    }
}
