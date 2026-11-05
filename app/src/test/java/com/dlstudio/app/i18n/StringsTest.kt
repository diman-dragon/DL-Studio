package com.dlstudio.app.i18n

import com.dlstudio.app.settings.AppLang
import com.dlstudio.app.settings.StorageStatus
import com.dlstudio.app.settings.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StringsTest {
    private fun texts(s: Strings): List<String> =
        Strings::class.java.methods.filter { it.parameterCount == 0 && it.returnType == String::class.java }.map { it.invoke(s) as String } +
            ThemeMode.values().map(s::themeName) + StorageStatus.values().filter { it != StorageStatus.NONE }.map(s::status)

    @Test fun noBlankTexts() {
        AppLang.values().forEach { lang -> texts(stringsFor(lang)).forEach { assertTrue(it.isNotBlank()) } }
    }

    @Test fun everyTextIsTranslatedExceptBrand() {
        val same = texts(StringsRu).zip(texts(StringsEn)).filter { it.first == it.second }.map { it.first }
        assertEquals(listOf("DL Studio"), same)
    }

    @Test fun noneStatusIsEmpty() {
        assertEquals("", StringsRu.status(StorageStatus.NONE))
        assertEquals("", StringsEn.status(StorageStatus.NONE))
    }
}
