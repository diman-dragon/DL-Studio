package com.dlstudio.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dlstudio.app.i18n.stringsFor
import com.dlstudio.app.library.LibraryViewModel
import com.dlstudio.app.settings.AccentColor
import com.dlstudio.app.settings.ThemeMode
import com.dlstudio.app.study.SpeechLang
import com.dlstudio.app.ui.theme.DlTheme
import com.dlstudio.app.ui.theme.LocalSpeakSlow
import com.dlstudio.app.ui.theme.LocalSpeechTag
import com.dlstudio.app.ui.theme.LocalStudyOptions
import com.dlstudio.app.ui.theme.LocalStrings

/** Корень интерфейса: тема и язык из состояния, дальше — каркас с экранами. */
@Composable
fun AppRoot(vm: AppViewModel, onFolder: () -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    val library: LibraryViewModel = viewModel()
    val libraryState by library.state.collectAsStateWithLifecycle()
    val speakSlow = remember(vm) { { text: String -> vm.speakSlow(text) } }
    val accent = if (libraryState.stats.overdueCards > 0) AccentColor.RED else state.accent
    CompositionLocalProvider(LocalStrings provides stringsFor(state.lang), LocalSpeechTag provides SpeechLang.fromVoice(state.speechVoiceName), LocalStudyOptions provides state.study, LocalSpeakSlow provides speakSlow) {
        DlTheme(dark = state.theme == ThemeMode.DARK, accent = accent, fonts = state.fonts) { AppScaffold(state, vm, onFolder) }
    }
}
