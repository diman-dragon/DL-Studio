package com.dlstudio.app.ui

import androidx.compose.runtime.Composable
import com.dlstudio.app.library.LibNode
import com.dlstudio.app.ui.screens.LibraryScreen
import com.dlstudio.app.ui.screens.SettingsScreen
import com.dlstudio.app.ui.screens.StatsScreen
import com.dlstudio.app.ui.screens.StudyScreen

/** Экран по номеру вкладки (порядок как у `Tabs.all`); `onStudy` — «Учить» из библиотеки: открывает сессию на первой вкладке. */
@Composable
fun AppPage(index: Int, state: AppState, vm: AppViewModel, onFolder: () -> Unit, onStudy: (List<LibNode>) -> Unit) {
    when (index) {
        0 -> StudyScreen(state.sessionSize, vm::speak)
        1 -> StatsScreen()
        2 -> LibraryScreen(onStudy = onStudy, onSpeak = vm::speak)
        else -> SettingsScreen(state, vm, onFolder)
    }
}
