package com.dlstudio.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dlstudio.app.i18n.stringsFor
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
    val speakSlow = remember(vm) { { text: String -> vm.speakSlow(text) } }
    CompositionLocalProvider(LocalStrings provides stringsFor(state.lang), LocalSpeechTag provides SpeechLang.fromVoice(state.speechVoiceName), LocalStudyOptions provides state.study, LocalSpeakSlow provides speakSlow) {
        DlTheme(dark = state.theme == ThemeMode.DARK, accent = state.accent, fonts = state.fonts) { AppScaffold(state, vm, onFolder) }
    }
}
