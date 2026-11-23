package com.dlstudio.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dlstudio.app.settings.AppLang
import com.dlstudio.app.speech.PackState
import com.dlstudio.app.speech.VoiceChoice
import com.dlstudio.app.speech.VoiceNames
import com.dlstudio.app.ui.AppState
import com.dlstudio.app.ui.AppViewModel
import com.dlstudio.app.ui.theme.LocalStrings
import java.util.Locale

/** Сколько голосов языка показывается сразу; остальные — по кнопке «Показать все». */
private const val VOICES_SHOWN = 6

/** Варианты скорости речи. */
private val RATES = listOf(0.7f, 1f, 1.15f)

/**
 * Раздел «Озвучка и распознавание речи»: голоса встроенного синтезатора Android с человеческими именами (выбор, прослушивание, скачивание),
 * скорость речи, офлайн-пакеты распознавания речи для проверки произношения. Ничего своего: всё делают системные движки.
 */
@Composable
fun VoiceSection(state: AppState, vm: AppViewModel) {
    val s = LocalStrings.current
    val ru = state.lang == AppLang.RU
    val tags = remember(state.topics) { state.topics.map { it.tag }.filter { it.isNotBlank() }.distinct().ifEmpty { listOf(DEFAULT_TAG) } }
    LaunchedEffect(tags) { vm.loadVoices(tags) }
    val shown = Locale.forLanguageTag(if (ru) "ru" else "en")
    SettingsSection(s.sectionVoice) {
        Text(s.voiceIntro, Modifier.padding(horizontal = 6.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        CycleTile(s.voiceRate, listOf(s.rateSlow to RATES[0], s.rateNormal to RATES[1], s.rateFast to RATES[2]), state.speech.rate, vm::setSpeechRate)
        tags.map { VoiceNames.language(it) }.distinct().forEach { lang -> LanguageVoices(lang, shown, state, vm) }
        SettingRow {
            ActionTile(s.voicesGet, vm::installVoices, Modifier.weight(1f))
            ActionTile(s.voicesSettings, vm::openSpeechSettings, Modifier.weight(1f))
        }
        ActionTile(s.voicesRefresh, { vm.loadVoices(tags) })
        Text(s.recogTitle, Modifier.padding(start = 6.dp, top = 8.dp), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
        Text(s.recogHint, Modifier.padding(horizontal = 6.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        tags.forEach { tag -> PackRow(shown, tag, state.speech.packs[tag] ?: PackState.UNKNOWN) { vm.downloadPack(tag) } }
    }
}

/** Голоса одного языка: «Авто» и список голосов с именами; ещё не скачанные помечены и скачиваются через настройки движка. */
@Composable
private fun LanguageVoices(lang: String, shown: Locale, state: AppState, vm: AppViewModel) {
    val s = LocalStrings.current
    val speech = state.speech
    var all by remember(lang) { mutableStateOf(false) }
    val voices = remember(speech.voices, lang, shown) { VoiceNames.choices(speech.voices, lang, shown.language == "ru") }
    val current = speech.chosen[lang]
    Text(
        Locale.forLanguageTag(lang).getDisplayLanguage(shown).replaceFirstChar { it.uppercase() }, Modifier.padding(start = 6.dp, top = 8.dp),
        style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary
    )
    when {
        !speech.loaded -> Text(s.voicesLoading, Modifier.padding(horizontal = 6.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        voices.isEmpty() -> Text(s.voicesEmpty, Modifier.padding(horizontal = 6.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        else -> {
            VoiceRow(s.voiceAuto, s.voiceAutoHint, current == null, null, { vm.chooseVoice(lang, null) })
            (if (all) voices else voices.take(VOICES_SHOWN)).forEach { v ->
                VoiceRow(v.title, details(v), current == v.name, v.takeIf { it.installed }, { if (v.installed) vm.chooseVoice(lang, v.name) else vm.installVoices() }) { text ->
                    vm.previewVoice(v, text)
                }
            }
            if (voices.size > VOICES_SHOWN) ActionTile(if (all) s.voicesLess else s.voicesAll(voices.size), { all = !all })
        }
    }
}

@Composable
private fun details(v: VoiceChoice): String {
    val s = LocalStrings.current
    return listOfNotNull(v.region.ifBlank { null }, s.voiceHigh.takeIf { v.high }, s.voiceNetwork.takeIf { v.network }, s.voiceNotInstalled.takeIf { !v.installed }).joinToString(" · ")
}

/** Строка голоса: переключатель, имя с подписью и, если голос скачан, кнопка прослушивания (`onPreview` получает пробную фразу). */
@Composable
private fun VoiceRow(title: String, subtitle: String, selected: Boolean, previewFor: VoiceChoice?, onSelect: () -> Unit, onPreview: (String) -> Unit = {}) {
    SettingCard(onClick = onSelect, gap = 2.dp) {
        Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(10.dp), Alignment.CenterVertically) {
            RadioButton(selected, null)
            Column(Modifier.weight(1f), Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                if (subtitle.isNotBlank()) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (previewFor != null) SmallSpeaker(sample(previewFor), { onPreview(it) }, size = 40.dp)
        }
    }
}

/** Пробная фраза голоса на его языке. */
private fun sample(v: VoiceChoice): String = when (VoiceNames.language(v.tag)) {
    "ru" -> "Привет! Меня зовут ${v.title}."
    "en" -> "Hello! My name is ${v.title}."
    else -> "Hello! ${v.title}."
}

/** Состояние офлайн-пакета распознавания речи для языка темы и действие: скачать (Android 13+) или открыть настройки голосового ввода. */
@Composable
private fun PackRow(shown: Locale, tag: String, state: PackState, onAction: () -> Unit) {
    val s = LocalStrings.current
    val (status, action) = when (state) {
        PackState.INSTALLED -> s.packInstalled to null
        PackState.AVAILABLE -> s.packAvailable to s.download
        PackState.DOWNLOADING -> s.packDownloading to null
        PackState.UNSUPPORTED -> s.packUnsupported to s.openSettings
        PackState.UNKNOWN -> s.packUnknown to s.openSettings
    }
    SettingCard(onClick = if (action == null) null else onAction, gap = 2.dp) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Column(Modifier.weight(1f), Arrangement.spacedBy(2.dp)) {
                Text(Locale.forLanguageTag(tag).getDisplayName(shown).replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                Text(status, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (state == PackState.DOWNLOADING) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
            else if (action != null) Text(action, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        }
    }
}

/** Язык озвучки, если ни у одной установленной темы его нет: английский. */
private const val DEFAULT_TAG = "en-US"
