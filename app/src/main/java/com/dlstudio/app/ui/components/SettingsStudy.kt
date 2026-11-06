package com.dlstudio.app.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dlstudio.app.ui.AppState
import com.dlstudio.app.ui.AppViewModel
import com.dlstudio.app.ui.theme.LocalStrings

/** Раздел «Учёба»: размер сессии и напоминания; `denied` — разрешение на уведомления не выдано. */
@Composable
fun SettingsStudy(state: AppState, vm: AppViewModel, denied: Boolean, onReminders: (Boolean) -> Unit) {
    val s = LocalStrings.current
    SettingsSection(s.sectionStudy) {
        OptionChips(s.dailyCards, listOf(10, 20, 30, 50).map { "$it" to it }, state.sessionSize, vm::setSessionSize)
        SwitchTile(s.reminders, if (state.remindersEnabled) s.on else s.off, state.remindersEnabled, onReminders)
        if (denied) Text(s.notificationsNeeded, Modifier.padding(horizontal = 6.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        if (state.remindersEnabled) OptionChips(s.reminderTime, listOf(8, 12, 18, 19, 21).map { "%02d:00".format(it) to it }, state.reminderHour, vm::setReminderHour)
    }
}
