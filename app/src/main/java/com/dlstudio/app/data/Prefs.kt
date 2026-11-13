package com.dlstudio.app.data

import android.content.Context
import android.content.SharedPreferences
import com.dlstudio.app.settings.AccentColor
import com.dlstudio.app.settings.AppLang
import com.dlstudio.app.settings.FontChoice
import com.dlstudio.app.settings.FontGroup
import com.dlstudio.app.settings.FontPreset
import com.dlstudio.app.settings.FontSettings
import com.dlstudio.app.settings.FontSpec
import com.dlstudio.app.settings.ThemeMode
import com.dlstudio.app.study.CardMode
import com.dlstudio.app.study.StudyOptions
import java.util.Locale

/** Настройки приложения (SharedPreferences). */
class Prefs(context: Context) {
    private val sp = context.getSharedPreferences("dlstudio", Context.MODE_PRIVATE)
    private val systemLang = if (Locale.getDefault().language == "ru") AppLang.RU else AppLang.EN

    var theme: ThemeMode
        get() = sp.getEnum("theme", ThemeMode.DARK)
        set(value) = sp.putPref("theme", value.name)

    var lang: AppLang
        get() = sp.getEnum("lang", systemLang)
        set(value) = sp.putPref("lang", value.name)

    var accent: AccentColor
        get() = sp.getEnum("accent", AccentColor.VIOLET)
        set(value) = sp.putPref("accent", value.name)

    /** Дерево SAF, которое выбрал пользователь; null — папка приложения по умолчанию. */
    var storageUri: String?
        get() = sp.getString("storage", null)
        set(value) = sp.putPref("storage", value)

    /** Вид библиотеки: true — карточки, false — список. */
    var libraryGrid: Boolean
        get() = sp.getBoolean("libraryGrid", true)
        set(value) { sp.edit().putBoolean("libraryGrid", value).apply() }

    var speechVoiceName: String?
        get() = sp.getString("speechVoice", null)
        set(value) = sp.putPref("speechVoice", value)

    /** Человеческое имя выбранного голоса — ровно то, что пользователь выбрал в списке. */
    var speechVoiceLabel: String?
        get() = sp.getString("speechVoiceLabelV2", null)
        set(value) = sp.putPref("speechVoiceLabelV2", value)

    /** Размер одной учебной сессии: 10, 50 или своё число. */
    var sessionSize: Int get() = sp.getInt("dailyCards", 20); set(value) { sp.edit().putInt("dailyCards", value).apply() }
    /** Снимок расписания для напоминаний (см. `ReminderSnapshot`). */
    var reminderSnapshot: String?
        get() = sp.getString("reminderSnapshot", null)
        set(value) = sp.putPref("reminderSnapshot", value)
    var remindersEnabled: Boolean get() = sp.getBoolean("remindersEnabled", false); set(value) { sp.edit().putBoolean("remindersEnabled", value).apply() }
    var reminderHour: Int get() = sp.getInt("reminderHour", 19); set(value) { sp.edit().putInt("reminderHour", value).apply() }
    var reminderMinute: Int get() = sp.getInt("reminderMinute", 0); set(value) { sp.edit().putInt("reminderMinute", value).apply() }
}

/** Настройки учёбы в тех же SharedPreferences, что и `Prefs`: режимы карточек, доля переворотов, проверка произношения, поздравления. */
class StudyPrefs(context: Context) {
    private val sp = context.getSharedPreferences("dlstudio", Context.MODE_PRIVATE)

    /** Выключенные режимы; у тех, кто сохранял прежний список включённых, новые режимы остаются включёнными. */
    private fun disabled(): Set<String> {
        sp.getString("modesOff", null)?.let { return it.split(',').toSet() }
        val saved = sp.getString("modes", null)?.split(',') ?: return emptySet()
        return LEGACY.filter { it !in saved }.toSet()
    }

    fun load(): StudyOptions {
        val off = disabled()
        val modes = CardMode.studied.filter { it.name !in off }.toSet().ifEmpty { CardMode.studied }
        return StudyOptions(modes, sp.getBoolean("pronounce", true), sp.getBoolean("celebrate", true), sp.getInt("flipShare", StudyOptions.DEFAULT_FLIP_SHARE),
            sp.getInt("slowRate", StudyOptions.DEFAULT_SLOW_RATE).takeIf { it in StudyOptions.SLOW_RATES } ?: StudyOptions.DEFAULT_SLOW_RATE,
            sp.getInt("slowPause", StudyOptions.DEFAULT_SLOW_PAUSE).takeIf { it in StudyOptions.SLOW_PAUSES } ?: StudyOptions.DEFAULT_SLOW_PAUSE)
    }

    fun save(options: StudyOptions) {
        sp.edit().putString("modesOff", CardMode.studied.filter { it !in options.modes }.joinToString(",") { it.name })
            .putBoolean("pronounce", options.pronounce).putBoolean("celebrate", options.celebrate).putInt("flipShare", options.flipShare)
            .putInt("slowRate", options.slowRate).putInt("slowPause", options.slowPause).apply()
    }

    private companion object { val LEGACY = listOf("CHOICE_FWD", "CHOICE_REV", "LISTEN", "WRITE_FWD", "WRITE_REV") }
}

/** Шрифты групп текста в тех же SharedPreferences, что и `Prefs`: семейство и размер на каждую группу. */
class FontPrefs(context: Context) {
    private val sp = context.getSharedPreferences("dlstudio", Context.MODE_PRIVATE)

    fun load(): FontSettings = FontGroup.values().fold(FontSettings(sp.getEnum("fontPreset", FontPreset.SYSTEM))) { all, group ->
        val choice = sp.getEnum("font_${group.name}", FontChoice.SANS)
        val scale = sp.getInt("fontScale_${group.name}", FontSpec.DEFAULT).coerceIn(FontSpec.SCALES.min(), FontSpec.SCALES.max())
        all.changed(group, FontSpec(choice, scale))
    }

    fun savePreset(preset: FontPreset) { sp.edit().putString("fontPreset", preset.name).apply() }

    /** Забывает режим и шрифты всех групп: дальше действуют значения по умолчанию. */
    fun reset() {
        val edit = sp.edit().remove("fontPreset")
        FontGroup.values().forEach { edit.remove("font_${it.name}").remove("fontScale_${it.name}") }
        edit.apply()
    }

    fun save(group: FontGroup, spec: FontSpec) {
        sp.edit().putString("font_${group.name}", spec.choice.name).putInt("fontScale_${group.name}", spec.scale).apply()
    }
}

/** Значение перечисления из настроек; нет или повреждено — `default`. */
inline fun <reified T : Enum<T>> SharedPreferences.getEnum(key: String, default: T): T {
    val saved = getString(key, null)
    return enumValues<T>().firstOrNull { it.name == saved } ?: default
}

/** Записывает строку (или удаляет ключ при null). */
fun SharedPreferences.putPref(key: String, value: String?) {
    edit().putString(key, value).apply()
}
