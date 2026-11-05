package com.dlstudio.app.ui.screens

import androidx.compose.runtime.Composable
import com.dlstudio.app.ui.components.StubScreen
import com.dlstudio.app.ui.theme.LocalStrings

@Composable
fun StatsScreen() {
    val s = LocalStrings.current
    StubScreen(s.tabStats, s.stubHint)
}
