package com.dlstudio.app.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dlstudio.app.settings.AppLang
import com.dlstudio.app.speech.Gender
import com.dlstudio.app.speech.Characters
import com.dlstudio.app.speech.PackState
import com.dlstudio.app.speech.Role
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

/**
 * Раздел «Языки и озвучка». Языки берутся из установленных тем: для каждого видно, чего не хватает (голос, женский или мужской голос для диалогов,
 * пакет распознавания), и есть кнопка «Скачать недостающее». Для выбранного языка — выпадающие списки: основной голос, женский и мужской голос диалогов,
 * пакеты распознавания. Не скачанные голоса и пакеты скачиваются по нажатию. Ничего своего: всё делают системные движки.
 * Список голосов обновляется при возвращении в приложение (например, после загрузки голоса).
 */
@Composable
fun VoiceSection(state: AppState, vm: AppViewModel) {
    val s = LocalStrings.current
    val shown = Locale.forLanguageTag(if (state.lang == AppLang.RU) "ru" else "en")
    val tags = remember(state.topics) { state.topics.map { it.tag }.filter { it.isNotBlank() }.distinct() }
    val langs = remember(tags) { tags.map { VoiceNames.language(it) }.distinct() }
    var picked by rememberSaveable { mutableStateOf("") }
    LoadVoicesOnResume(tags, vm)
    SettingsSection(s.sectionVoice) {
        Text(s.voiceIntro, Modifier.padding(horizontal = 6.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (langs.isEmpty()) {
            Text(s.statusNoLangs, Modifier.padding(horizontal = 6.dp), style = MaterialTheme.typography.bodyMedium)
            return@SettingsSection
        }
        val lang = picked.takeIf { it in langs } ?: langs.first()
        LangStatus(state, vm, shown)
        CycleTile(s.voiceRate, listOf(s.rateSlow to RATES[0], s.rateNormal to RATES[1], s.rateFast to RATES[2]), state.speech.rate, vm::setSpeechRate)
        if (langs.size > 1) PickerTile(s.voiceLanguage, languageName(lang, shown), langs.map { PickItem(it, languageName(it, shown), selected = it == lang) }, { picked = it })
        Text(s.teachersTitle, Modifier.padding(start = 6.dp, top = 4.dp), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
        TeacherHeader(Gender.F, s.teacherFName, s.teacherFInfo) { greet("grace", lang, tags, vm) }
        VoicePicker(s.teacherFName, lang, "$lang:grace", Gender.F, shown, state, vm)
        TeacherHeader(Gender.M, s.teacherMName, s.teacherMInfo) { greet("leo", lang, tags, vm) }
        VoicePicker(s.teacherMName, lang, "$lang:leo", Gender.M, shown, state, vm)
        Text("Другие персонажи", Modifier.padding(start = 6.dp, top = 6.dp), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
        VoicePicker("Beatrice", lang, "$lang:beatrice", Gender.F, shown, state, vm)
        VoicePicker("Sergei", lang, "$lang:sergei", Gender.M, shown, state, vm)
        Text(s.voiceGenderHint, Modifier.padding(horizontal = 6.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        PackPicker(tags.filter { VoiceNames.language(it) == lang }, shown, state, vm)
    }
}

/** Преподаватель здоровается на языке тем своим голосом (по нажатию на его аватар). */
private fun greet(characterId: String, lang: String, tags: List<String>, vm: AppViewModel) {
    val profile = Characters.get(characterId) ?: return
    val gender = profile.gender
    val tag = tags.firstOrNull { VoiceNames.language(it) == lang } ?: return
    val text = when (lang) {
        "ru" -> "Здравствуйте! Меня зовут ${profile.name}."
        "it" -> "Ciao! Mi chiamo ${profile.name}."
        else -> "Hello! My name is ${profile.name}."
    }
    vm.speak(text, tag, Role.character(characterId))
}

fun languageName(lang: String, shown: Locale) = Locale.forLanguageTag(lang).getDisplayLanguage(shown).replaceFirstChar { it.uppercase() }

/**
 * Голос преподавателя: «Авто» (самый качественный) и голоса своего пола с именами; `key` — «en:f» (учительница) или «en:m» (преподаватель).
 * Не скачанный голос скачивается через экран загрузки голосов самого движка.
 */
@Composable
private fun VoicePicker(title: String, lang: String, key: String, gender: Int, shown: Locale, state: AppState, vm: AppViewModel) {
    val s = LocalStrings.current
    val speech = state.speech
    val voices = remember(speech.voices, lang, shown, gender) { VoiceNames.choices(speech.voices, lang, shown.language == "ru").filter { it.gender == gender } }
    val current = speech.chosen[key]
    val value = when {
        !speech.loaded -> s.voicesLoading
        else -> voices.firstOrNull { it.name == current }?.title ?: s.voiceAuto
    }
    val items = buildList {
        add(PickItem(KEY_AUTO, s.voiceAuto, s.genderAutoHint, selected = current == null))
        voices.forEach { v ->
            val preview = v.takeIf { it.installed }?.let { voice -> { text: String -> vm.previewVoice(voice, text) } }
            add(PickItem(v.name, v.title, details(v), v.name == current, if (v.installed) "" else s.download, preview, sample(v)))
        }
        add(PickItem(KEY_MORE, s.voicesMore))
    }
    PickerTile(title, value, items, { picked ->
        when (picked) {
            KEY_AUTO -> vm.chooseVoice(key, null)
            KEY_MORE -> vm.downloadVoice()
            else -> voices.firstOrNull { it.name == picked }?.let { if (it.installed) vm.chooseVoice(key, it.name) else vm.downloadVoice() }
        }
    })
}

@Composable
private fun details(v: VoiceChoice): String {
    val s = LocalStrings.current
    val gender = if (v.gender == Gender.F) s.genderFemale else s.genderMale
    return listOfNotNull(gender, v.region.ifBlank { null }, s.voiceHigh.takeIf { v.high }, s.voiceNetwork.takeIf { v.network }, s.voiceNotInstalled.takeIf { !v.installed }).joinToString(" · ")
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
