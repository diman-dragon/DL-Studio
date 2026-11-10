package com.dlstudio.app.ui.components

import androidx.compose.runtime.Composable
import com.dlstudio.app.study.CardMode
import com.dlstudio.app.ui.theme.LocalStrings

/** «Режимы карточек» в тестовом режиме: единственный включённый режим (просмотр с переворотом, не выключается) и свёрнутый список остальных. */
@Composable
fun SettingsFlipOnly() {
    val s = LocalStrings.current
    SettingsSection(s.sectionModes) {
        SwitchTile(s.modeName(CardMode.FLIP_FWD), s.modeFlipHint, true, {}, enabled = false)
        SoonModes()
    }
}
