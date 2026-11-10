package com.dlstudio.app.ui.components

import com.dlstudio.app.Features
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dlstudio.app.study.CardMode
import com.dlstudio.app.ui.AppState
import com.dlstudio.app.ui.AppViewModel
import com.dlstudio.app.ui.theme.LocalStrings
import kotlin.math.abs

/** Разделы «Частота показов» (доля карточек с переворотом), «Режимы карточек» (хотя бы один включён) и «Помощники» (микрофон, поздравления); при `Features.FLIP_ONLY` частоты нет, а режим один. */
@Composable
fun SettingsModes(state: AppState, vm: AppViewModel) {
    val s = LocalStrings.current
    val study = state.study
    val shares = listOf(30, 50, 70, 90)
    fun label(on: Boolean) = if (on) s.on else s.off
    if (Features.FLIP_ONLY) SettingsFlipOnly() else {
        SettingsSection(s.sectionFrequency) {
            OptionChips(s.flipShare, shares.map { "$it%" to it }, shares.minByOrNull { abs(it - study.flipShare) } ?: 50, { vm.setStudy(study.copy(flipShare = it)) })
            Text(s.flipShareHint, Modifier.padding(horizontal = 6.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        SettingsSection(s.sectionModes) {
            CardMode.studied.forEach { mode ->
                val on = mode in study.modes
                val last = study.isLastMode(mode)
                SwitchTile(s.modeName(mode), if (last) s.modeLast else label(on), on, { vm.setStudy(study.withMode(mode, it)) }, enabled = !last)
            }
        }
    }
    SettingsSection(s.sectionHelpers) {
        SwitchTile(s.pronounceSetting, label(study.pronounce), study.pronounce, { vm.setStudy(study.copy(pronounce = it)) })
        SwitchTile(s.celebrateSetting, label(study.celebrate), study.celebrate, { vm.setStudy(study.copy(celebrate = it)) })
    }
}
