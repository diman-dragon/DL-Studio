package com.dlstudio.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dlstudio.app.ui.components.CrashDialog
import com.dlstudio.app.ui.components.LocalSpeaking
import com.dlstudio.app.i18n.stringsFor
import com.dlstudio.app.settings.ThemeMode
import com.dlstudio.app.ui.theme.DlTheme
import com.dlstudio.app.ui.theme.LocalStrings

/** Корень: язык и тема из настроек, затем каркас с вкладками. */
@Composable
fun AppRoot(vm: AppViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    val speaking by vm.speaking.collectAsStateWithLifecycle()
    CompositionLocalProvider(LocalStrings provides stringsFor(state.lang), LocalSpeaking provides speaking) {
        DlTheme(state.theme == ThemeMode.DARK, state.accent) {
            AppScaffold(state, vm)
            state.crash?.let { CrashDialog(it, vm::dismissCrash) }
        }
    }
}
