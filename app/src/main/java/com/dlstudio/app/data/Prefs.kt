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
