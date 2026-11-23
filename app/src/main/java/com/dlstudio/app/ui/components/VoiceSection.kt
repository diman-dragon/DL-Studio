package com.dlstudio.app.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.dlstudio.app.settings.AppLang
import com.dlstudio.app.speech.PackState
import com.dlstudio.app.speech.VoiceChoice
import com.dlstudio.app.speech.VoiceNames
import com.dlstudio.app.ui.AppState
import com.dlstudio.app.ui.AppViewModel
import com.dlstudio.app.ui.theme.LocalStrings
import java.util.Locale

/** Варианты скорости речи. */
private val RATES = listOf(0.7f, 1f, 1.15f)

/** Служебные ключи пунктов списка голосов. */
private const val KEY_AUTO = "\u0000auto"
private const val KEY_MORE = "\u0000more"

/** Язык озвучки, если ни у одной установленной темы его нет: английский. */
private const val DEFAULT_TAG = "en-US"

/**
 * Раздел «Озвучка и распознавание речи». Язык выбирается в выпадающем списке; для него — список голосов встроенного синтезатора с человеческими
 * именами и список пакетов распознавания речи. Пункты не скачанных голосов и пакетов помечены «Скачать» и скачиваются по нажатию, без захода в общие настройки системы.
 * Ничего своего: всё делают системные движки. Список голосов обновляется при возвращении в приложение (например, после загрузки голоса).
 */
@Composable
fun VoiceSection(state: AppState, vm: AppViewModel) {
    val s = LocalStrings.current
    val shown = Locale.forLanguageTag(if (state.lang == AppLang.RU) "ru" else "en")
    val tags = remember(state.topics) { state.topics.map { it.tag }.filter { it.isNotBlank() }.distinct().ifEmpty { listOf(DEFAULT_TAG) } }
    val langs = remember(tags) { tags.map { VoiceNames.language(it) }.distinct() }
    var picked by rememberSaveable { mutableStateOf("") }
    val lang = picked.takeIf { it in langs } ?: langs.first()
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner, tags) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) vm.loadVoices(tags) }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
    SettingsSection(s.sectionVoice) {
        Text(s.voiceIntro, Modifier.padding(horizontal = 6.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        CycleTile(s.voiceRate, listOf(s.rateSlow to RATES[0], s.rateNormal to RATES[1], s.rateFast to RATES[2]), state.speech.rate, vm::setSpeechRate)
        PickerTile(s.voiceLanguage, languageName(lang, shown), langs.map { PickItem(it, languageName(it, shown), selected = it == lang) }, { picked = it })
        VoicePicker(lang, shown, state, vm)
        PackPicker(tags.filter { VoiceNames.language(it) == lang }, shown, state, vm)
    }
}

private fun languageName(lang: String, shown: Locale) = Locale.forLanguageTag(lang).getDisplayLanguage(shown).replaceFirstChar { it.uppercase() }

/** Голос выбранного языка: «Авто» и голоса с именами; не скачанный голос скачивается через экран загрузки голосов самого движка. */
@Composable
private fun VoicePicker(lang: String, shown: Locale, state: AppState, vm: AppViewModel) {
    val s = LocalStrings.current
    val speech = state.speech
    val voices = remember(speech.voices, lang, shown) { VoiceNames.choices(speech.voices, lang, shown.language == "ru") }
    val current = speech.chosen[lang]
    val value = when {
        !speech.loaded -> s.voicesLoading
        else -> voices.firstOrNull { it.name == current }?.title ?: s.voiceAuto
    }
    val items = buildList {
        add(PickItem(KEY_AUTO, s.voiceAuto, s.voiceAutoHint, selected = current == null))
        voices.forEach { v ->
            val preview = v.takeIf { it.installed }?.let { voice -> { text: String -> vm.previewVoice(voice, text) } }
            add(PickItem(v.name, v.title, details(v), v.name == current, if (v.installed) "" else s.download, preview, sample(v)))
        }
        add(PickItem(KEY_MORE, s.voicesMore))
    }
    PickerTile(s.voiceLabel, value, items, { key ->
        when (key) {
            KEY_AUTO -> vm.chooseVoice(lang, null)
            KEY_MORE -> vm.downloadVoice()
            else -> voices.firstOrNull { it.name == key }?.let { if (it.installed) vm.chooseVoice(lang, it.name) else vm.downloadVoice() }
        }
    })
}

@Composable
private fun details(v: VoiceChoice): String {
    val s = LocalStrings.current
    return listOfNotNull(v.region.ifBlank { null }, s.voiceHigh.takeIf { v.high }, s.voiceNetwork.takeIf { v.network }, s.voiceNotInstalled.takeIf { !v.installed }).joinToString(" · ")
}

/** Пробная фраза голоса на его языке. */
private fun sample(v: VoiceChoice): String = when (VoiceNames.language(v.tag)) {
    "ru" -> "Привет! Меня зовут ${v.title}."
    "en" -> "Hello! My name is ${v.title}."
    else -> "Hello! ${v.title}."
}

/**
 * Пакеты распознавания речи для языка — в том же выпадающем списке, что и голоса. Нажатие на нескачанный пакет скачивает его (Android 13+),
 * на старых Android открывается экран офлайн-языков голосового ввода.
 */
@Composable
private fun PackPicker(tags: List<String>, shown: Locale, state: AppState, vm: AppViewModel) {
    val s = LocalStrings.current
    val states = tags.associateWith { state.speech.packs[it] ?: PackState.UNKNOWN }
    val value = when (states.values.firstOrNull()) {
        PackState.INSTALLED -> s.packOn
        PackState.AVAILABLE -> s.packOff
        PackState.DOWNLOADING -> s.packDownloading
        else -> s.openSettings
    }
    val items = tags.map { tag ->
        val st = states.getValue(tag)
        val note = when (st) {
            PackState.INSTALLED -> s.packInstalled
            PackState.AVAILABLE -> s.packAvailable
            PackState.DOWNLOADING -> s.packDownloading
            PackState.UNSUPPORTED -> s.packUnsupported
            PackState.UNKNOWN -> s.packUnknown
        }
        val badge = when (st) { PackState.AVAILABLE -> s.download; PackState.UNSUPPORTED, PackState.UNKNOWN -> s.openSettings; else -> "" }
        PickItem(tag, Locale.forLanguageTag(tag).getDisplayName(shown).replaceFirstChar { it.uppercase() }, note, st == PackState.INSTALLED, badge)
    }
    PickerTile(s.recogTitle, value, items, { tag -> if (states[tag] != PackState.INSTALLED && states[tag] != PackState.DOWNLOADING) vm.downloadPack(tag) }, Modifier.fillMaxWidth())
}
