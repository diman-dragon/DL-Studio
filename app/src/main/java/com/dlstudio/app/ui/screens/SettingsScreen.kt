package com.dlstudio.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dlstudio.app.settings.AppLang
import com.dlstudio.app.settings.ThemeMode
import com.dlstudio.app.ui.AppState
import com.dlstudio.app.ui.AppViewModel
import com.dlstudio.app.ui.components.AboutCard
import com.dlstudio.app.ui.components.AccentTile
import com.dlstudio.app.ui.components.BackupSection
import com.dlstudio.app.ui.components.CycleTile
import com.dlstudio.app.ui.components.NeedsOffer
import com.dlstudio.app.ui.components.ResetSection
import com.dlstudio.app.ui.components.ScreenHeader
import com.dlstudio.app.ui.components.SettingRow
import com.dlstudio.app.ui.components.SettingsSection
import com.dlstudio.app.ui.components.ThemesSection
import com.dlstudio.app.ui.components.VoiceSection
import com.dlstudio.app.ui.theme.LocalStrings
import java.util.Locale

/** Варианты цели дня, XP. */
private val GOALS = listOf(10, 20, 30, 50, 100)

/** Настройки: внешний вид (тема, язык, цвет по радуге), цель дня, языки и озвучка (языки установленных тем, голоса по полу, пакеты распознавания), темы курсов (из файла, удаление), сброс прогресса и «О нас». */
@Composable
fun SettingsScreen(state: AppState, vm: AppViewModel) {
    val s = LocalStrings.current
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> if (uri != null) vm.installFile(uri) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), Arrangement.spacedBy(12.dp)) {
        ScreenHeader(s.tabSettings)
        SettingsSection(s.sectionLook) {
            SettingRow {
                CycleTile(s.settingTheme, ThemeMode.values().map { s.themeName(it) to it }, state.theme, vm::setTheme, Modifier.weight(1f))
                CycleTile(s.settingLanguage, AppLang.values().map { it.label to it }, state.lang, vm::setLang, Modifier.weight(1f))
            }
            AccentTile(s.settingAccent, state.accent, vm::setAccent)
        }
        SettingsSection(s.sectionLearning) {
            CycleTile(s.settingGoal, GOALS.map { s.goalValue(it) to it }, state.dailyGoal, vm::setDailyGoal)
        }
        ThemesSection(state, vm) { picker.launch(arrayOf("*/*")) }
        VoiceSection(state, vm)
        NeedsOffer(state, vm, Locale.forLanguageTag(if (state.lang == AppLang.RU) "ru" else "en"))
        BackupSection(vm::saveBackup, vm::restoreBackup)
        ResetSection(vm::resetProgress)
        AboutCard()
    }
}
