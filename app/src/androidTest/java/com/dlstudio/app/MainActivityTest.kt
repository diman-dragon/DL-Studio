package com.dlstudio.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

/** Минимальная smoke-проверка: приложение запускается и показывает навигацию. */
class MainActivityTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Test
    fun showsStudyTab() { rule.onNodeWithText("Учёба").assertIsDisplayed() }
}
