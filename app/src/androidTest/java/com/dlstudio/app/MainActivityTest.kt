package com.dlstudio.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import com.dlstudio.app.i18n.stringsFor
import com.dlstudio.app.settings.AppLang
import java.util.Locale
import org.junit.Rule
import org.junit.Test

/** Минимальная smoke-проверка: приложение запускается и показывает навигацию (язык — как у системы, как и в самом приложении). */
class MainActivityTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Test
    fun showsStudyTab() {
        val lang = if (Locale.getDefault().language == "ru") AppLang.RU else AppLang.EN
        rule.onAllNodesWithText(stringsFor(lang).tabStudy).onFirst().assertIsDisplayed()
    }
}
