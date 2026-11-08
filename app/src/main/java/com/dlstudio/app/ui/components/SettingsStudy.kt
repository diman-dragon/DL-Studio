package com.dlstudio.app.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dlstudio.app.ui.AppState
import com.dlstudio.app.ui.AppViewModel
import com.dlstudio.app.ui.theme.LocalStrings

/** Раздел «Учёба»: размер сессии (10, 50 или своё число — выбор запоминается) и напоминания; `denied` — разрешение на уведомления не выдано. */
@Composable
fun SettingsStudy(state: AppState, vm: AppViewModel, denied: Boolean, onReminders: (Boolean) -> Unit) {
    val s = LocalStrings.current
    var custom by remember { mutableStateOf(false) }
    val size = state.sessionSize
    val preset = size == 10 || size == 50
    val other = -1
    SettingsSection(s.sectionStudy) {
        OptionChips(
            s.dailyCards, listOf("10" to 10, "50" to 50, (if (preset) s.customSize else "${s.customSize}: $size") to other),
            if (preset) size else other, { if (it == other) custom = true else vm.setSessionSize(it) }
        )
        SwitchTile(s.reminders, if (state.remindersEnabled) s.on else s.off, state.remindersEnabled, onReminders)
        if (denied) Text(s.notificationsNeeded, Modifier.padding(horizontal = 6.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        if (state.remindersEnabled) OptionChips(s.reminderTime, listOf(8, 12, 18, 19, 21).map { "%02d:00".format(it) to it }, state.reminderHour, vm::setReminderHour)
    }
    if (custom) SessionSizeDialog(size, { vm.setSessionSize(it); custom = false }) { custom = false }
}
