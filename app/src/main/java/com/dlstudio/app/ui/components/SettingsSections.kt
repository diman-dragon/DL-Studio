package com.dlstudio.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dlstudio.app.settings.AppLang
import com.dlstudio.app.settings.FontGroup
import com.dlstudio.app.settings.FontPreset
import com.dlstudio.app.settings.FontSettings
import com.dlstudio.app.settings.StorageStatus
import com.dlstudio.app.settings.ThemeMode
import com.dlstudio.app.storage.StorageLayout
import com.dlstudio.app.study.CardMode
import com.dlstudio.app.study.SlowSpeech
import com.dlstudio.app.study.SpeechLang
import com.dlstudio.app.study.StudyOptions
import com.dlstudio.app.ui.AppState
import com.dlstudio.app.ui.AppViewModel
import com.dlstudio.app.ui.theme.LocalStrings
import java.util.Locale
import kotlin.math.abs

/** Раздел настроек: небольшой акцентный заголовок и плитки под ним. Все разделы выглядят одинаково. */
@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title.uppercase(), Modifier.padding(start = 6.dp, top = 8.dp), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        content()
    }
}

/** Раздел «Данные»: папка хранения с состоянием переноса и «О нас». */
@Composable
fun SettingsData(state: AppState, onFolder: () -> Unit) {
    val s = LocalStrings.current
    val moving = state.status == StorageStatus.MOVING
    SettingsSection(s.sectionData) {
        SettingTile(s.settingFolder, state.folderName?.let { "$it/${StorageLayout.ROOT}" } ?: s.folderDefault, onFolder, enabled = !moving)
        StatusText(s.status(state.status), moving)
        AboutCard()
    }
}

/** Раздел «Внешний вид»: тема и язык (плитки-кнопки по кругу), акцентный цвет. */
@Composable
fun SettingsLook(state: AppState, vm: AppViewModel) {
    val s = LocalStrings.current
    SettingsSection(s.sectionLook) {
        SettingRow {
            CycleTile(s.settingTheme, ThemeMode.values().map { s.themeName(it) to it }, state.theme, vm::setTheme, Modifier.weight(1f))
            CycleTile(s.settingLanguage, AppLang.values().map { it.label to it }, state.lang, vm::setLang, Modifier.weight(1f))
        }
        AccentTile(s.settingAccent, state.accent, s::accentName, vm::setAccent)
    }
}

/** Разделы частоты показов, режимов карточек (выбираются во всплывающем окне, как голос озвучки) и помощников. */
@Composable
fun SettingsModes(state: AppState, vm: AppViewModel) {
    val s = LocalStrings.current
    val study = state.study
    val shares = listOf(30, 50, 70, 90)
    var picking by remember { mutableStateOf(false) }
    fun label(on: Boolean) = if (on) s.on else s.off
    SettingsSection(s.sectionFrequency) {
        CycleTile(s.flipShare, shares.map { "$it%" to it }, shares.minByOrNull { abs(it - study.flipShare) } ?: 50, { vm.setStudy(study.copy(flipShare = it)) })
        Text(s.flipShareHint, Modifier.padding(horizontal = 6.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    SettingsSection(s.sectionModes) {
        SettingTile(s.sectionModes, s.modesEnabled(study.modes.size, CardMode.studied.size), { picking = true })
    }
    SettingsSection(s.sectionHelpers) {
        SwitchTile(s.pronounceSetting, label(study.pronounce), study.pronounce, { vm.setStudy(study.copy(pronounce = it)) })
        SwitchTile(s.celebrateSetting, label(study.celebrate), study.celebrate, { vm.setStudy(study.copy(celebrate = it)) })
    }
    if (picking) ModesPicker(study, { vm.setStudy(it) }) { picking = false }
}

/** Окно выбора режимов карточек: нажатие включает или выключает режим сразу; последний включённый выключить нельзя. */
@Composable
fun ModesPicker(study: StudyOptions, onChange: (StudyOptions) -> Unit, onClose: () -> Unit) {
    val s = LocalStrings.current
    val colors = MaterialTheme.colorScheme
    DlModal.Shell(s.sectionModes, onClose) {
        Column(Modifier.heightIn(max = 390.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            CardMode.studied.forEach { mode ->
                val on = mode in study.modes
                val last = study.isLastMode(mode)
                Surface(
                    Modifier.fillMaxWidth().toggleable(on, !last, Role.Checkbox) { onChange(study.withMode(mode, it)) }, RoundedCornerShape(16.dp),
                    if (on) colors.primaryContainer else colors.surfaceVariant,
                    border = BorderStroke(1.dp, if (on) colors.primary else colors.outlineVariant)
                ) {
                    Row(Modifier.padding(14.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(s.modeName(mode), style = MaterialTheme.typography.titleSmall)
                            if (last) Text(s.modeLast, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                        }
                        Text(if (on) "✓" else "", style = MaterialTheme.typography.titleMedium, color = colors.primary)
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth(), Arrangement.End) { DlModal.Action(s.close, onClose) }
    }
}

/** Раздел «Озвучка»: голос под его человеческим именем (так же, как в списке) и настройки «помедленнее»: темп, паузы и проверка на примере. */
@Composable
fun SettingsSpeech(state: AppState, ui: Locale, vm: AppViewModel, onVoice: () -> Unit) {
    val s = LocalStrings.current
    val name = state.speechVoiceName
    SettingsSection(s.sectionSpeech) {
        SettingTile(s.speechVoice, state.speechVoiceLabel ?: name?.let { VoiceName.fallback(it, ui) } ?: state.lang.label, onVoice)
        val o = state.study
        SettingRow {
            CycleTile(s.slowRate, StudyOptions.SLOW_RATES.map { "$it%" to it }, o.slowRate, { vm.setStudy(o.copy(slowRate = it)) }, Modifier.weight(1f))
            CycleTile(s.slowPause, StudyOptions.SLOW_PAUSES.map { s.slowPauseName(it) to it }, o.slowPause, { vm.setStudy(o.copy(slowPause = it)) }, Modifier.weight(1f))
        }
        val sample = SlowSpeech.sample(SpeechLang.fromVoice(state.speechVoiceName))
        SettingTile(s.slowTest, sample, { vm.speakSlow(sample) })
    }
}

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
        if (state.remindersEnabled) CycleTile(s.reminderTime, listOf(8, 12, 18, 19, 21).map { "%02d:00".format(it) to it }, state.reminderHour, vm::setReminderHour)
    }
    if (custom) SessionSizeDialog(size, { vm.setSessionSize(it); custom = false }) { custom = false }
}

/**
 * Раздел «Шрифты»: режим (системный, адаптивный, свой) с пояснением; в режиме «Свой» — шрифт и размер каждой группы;
 * «Вернуть по умолчанию» появляется, когда что-то изменено.
 */
@Composable
fun SettingsFonts(state: AppState, vm: AppViewModel) {
    val s = LocalStrings.current
    val fonts = state.fonts
    SettingsSection(s.sectionFonts) {
        OptionChips(s.fontPresetTitle, FontPreset.values().map { s.fontPresetName(it) to it }, fonts.preset, vm::setFontPreset)
        Text(s.fontPresetHint(fonts.preset), Modifier.padding(horizontal = 6.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (fonts.preset == FontPreset.CUSTOM) FontGroup.values().forEach { group -> FontTile(group, fonts[group]) { vm.setFont(group, it) } }
        if (fonts != FontSettings()) SettingTile(s.fontReset, s.fontResetHint, vm::resetFonts)
    }
}

/** Окно «Своё число карточек в сессии»: число от 1 до 500; пока оно неверное, «Сохранить» ничего не делает. */
@Composable
fun SessionSizeDialog(current: Int, onSave: (Int) -> Unit, onClose: () -> Unit) {
    val s = LocalStrings.current
    val colors = MaterialTheme.colorScheme
    val focus = remember { FocusRequester() }
    var text by remember { mutableStateOf(current.toString()) }
    val size = text.toIntOrNull()?.takeIf { it in 1..500 }
    LaunchedEffect(Unit) { focus.requestFocus() }
    DlModal.Shell(s.customSizeTitle, onClose) {
        Surface(Modifier.fillMaxWidth(), RoundedCornerShape(16.dp), colors.surfaceVariant, border = BorderStroke(1.5.dp, if (size == null) colors.error else colors.primary)) {
            Box(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), Alignment.Center) {
                BasicTextField(
                    text, { text = it.filter(Char::isDigit).take(3) }, Modifier.fillMaxWidth().focusRequester(focus), singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = MaterialTheme.typography.headlineMedium.copy(color = colors.onSurface, textAlign = TextAlign.Center),
                    cursorBrush = SolidColor(colors.primary)
                )
            }
        }
        Text(s.customSizeHint, color = if (size == null) colors.error else colors.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        DlModal.Actions(s.cancel, s.save, onClose, { size?.let(onSave) })
    }
}
