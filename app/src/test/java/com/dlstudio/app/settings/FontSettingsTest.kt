package com.dlstudio.app.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class FontSettingsTest {
    private val custom = FontSettings().withPreset(FontPreset.CUSTOM)

    @Test fun defaultsToSystemPresetWithSansAtFullSize() {
        assertEquals(FontPreset.SYSTEM, FontSettings().preset)
        FontGroup.values().forEach { assertEquals(FontSpec(FontChoice.SANS, 100), FontSettings()[it]) }
    }

    @Test fun changeAffectsOnlyThatGroup() {
        val changed = custom.changed(FontGroup.IPA, FontSpec(FontChoice.SERIF, 115))
        assertEquals(FontSpec(FontChoice.SERIF, 115), changed[FontGroup.IPA])
        assertEquals(FontSpec(), changed[FontGroup.CARD])
    }

    @Test fun lastChangeWins() {
        val twice = custom.changed(FontGroup.CARD, FontSpec(FontChoice.MONO)).changed(FontGroup.CARD, FontSpec(FontChoice.CURSIVE, 85))
        assertEquals(FontSpec(FontChoice.CURSIVE, 85), twice[FontGroup.CARD])
    }

    @Test fun groupSettingsApplyOnlyInCustomPreset() {
        val tuned = custom.changed(FontGroup.UI, FontSpec(FontChoice.SERIF, 130))
        assertEquals(FontSpec(), tuned.withPreset(FontPreset.ADAPTIVE)[FontGroup.UI])
        assertEquals(FontSpec(FontChoice.SERIF, 130), tuned.withPreset(FontPreset.SYSTEM).withPreset(FontPreset.CUSTOM)[FontGroup.UI])
    }

    @Test fun defaultScaleIsOneOfTheOffered() {
        assertEquals(true, FontSpec.DEFAULT in FontSpec.SCALES)
        assertNotEquals(FontSettings(), custom.changed(FontGroup.UI, FontSpec(scale = 130)))
    }

    @Test fun returningGroupToDefaultEqualsUntouchedSettings() {
        assertEquals(FontSettings(), FontSettings().changed(FontGroup.NOTE, FontSpec(FontChoice.MONO)).changed(FontGroup.NOTE, FontSpec()))
    }
}
