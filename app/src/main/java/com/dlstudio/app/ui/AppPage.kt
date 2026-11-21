package com.dlstudio.app.ui

import androidx.compose.runtime.Composable
import com.dlstudio.app.ui.screens.LibraryScreen
import com.dlstudio.app.ui.screens.SettingsScreen

/** Основные экраны приложения: библиотека материалов и настройки. */
@Composable
fun AppPage(index: Int, state: AppState, vm: AppViewModel, onFolder: () -> Unit) {
    when (index) {
        0 -> LibraryScreen(onSpeak = vm::speak)
        else -> SettingsScreen(state, vm, onFolder)
    }
}
