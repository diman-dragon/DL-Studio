package com.dlstudio.app.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class FontSettingsTest {
    @Test fun defaultsToSansAtFullSize() {
        FontGroup.values().forEach { assertEquals(FontSpec(FontChoice.SANS, 100), FontSettings()[it]) }
    }

    @Test fun changeAffectsOnlyThatGroup() {
        val changed = FontSettings().changed(FontGroup.IPA, FontSpec(FontChoice.SERIF, 115))
        assertEquals(FontSpec(FontChoice.SERIF, 115), changed[FontGroup.IPA])
        assertEquals(FontSpec(), changed[FontGroup.CARD])
    }

    @Test fun lastChangeWins() {
        val twice = FontSettings().changed(FontGroup.CARD, FontSpec(FontChoice.MONO)).changed(FontGroup.CARD, FontSpec(FontChoice.CURSIVE, 85))
        assertEquals(FontSpec(FontChoice.CURSIVE, 85), twice[FontGroup.CARD])
    }

    @Test fun defaultScaleIsOneOfTheOffered() {
        assertEquals(true, FontSpec.DEFAULT in FontSpec.SCALES)
        assertNotEquals(FontSettings(), FontSettings().changed(FontGroup.UI, FontSpec(scale = 130)))
    }
}
