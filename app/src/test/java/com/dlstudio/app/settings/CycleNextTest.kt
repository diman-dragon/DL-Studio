package com.dlstudio.app.settings

import org.junit.Assert.assertEquals
import org.junit.Test

class CycleNextTest {
    @Test fun themeCyclesBetweenTwoValues() {
        assertEquals(ThemeMode.LIGHT, ThemeMode.DARK.cycleNext())
        assertEquals(ThemeMode.DARK, ThemeMode.LIGHT.cycleNext())
    }

    @Test fun languageCyclesBetweenTwoValues() {
        assertEquals(AppLang.EN, AppLang.RU.cycleNext())
        assertEquals(AppLang.RU, AppLang.EN.cycleNext())
    }
}
