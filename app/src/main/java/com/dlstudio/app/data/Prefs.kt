package com.dlstudio.app.data

import android.content.Context
import android.content.SharedPreferences
import com.dlstudio.app.settings.AccentColor
import com.dlstudio.app.settings.AppLang
import com.dlstudio.app.settings.ThemeMode
import java.util.Locale

/** Настройки и прогресс (SharedPreferences). Прогресс хранится одной JSON-строкой: см. progress/Progress.kt. */
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
        get() = AccentColor(sp.getFloat("accentHue", AccentColor.DEFAULT.hue))
        set(value) { sp.edit().putFloat("accentHue", value.hue).apply() }

    /** Цель дня в XP. */
    var dailyGoal: Int
        get() = sp.getInt("dailyGoal", DEFAULT_GOAL)
        set(value) { sp.edit().putInt("dailyGoal", value).apply() }

    /** Адрес папки с course.json; пусто — каталог не задан. */
    var catalogUrl: String
        get() = sp.getString("catalogUrl", "").orEmpty()
        set(value) = sp.putPref("catalogUrl", value.trim())

    /** Встроенные темы уже скопированы при первом запуске (удалённые не возвращаются). */
    var seeded: Boolean
        get() = sp.getBoolean("seeded", false)
        set(value) { sp.edit().putBoolean("seeded", value).apply() }

    var progressJson: String?
        get() = sp.getString("progress", null)
        set(value) = sp.putPref("progress", value)
}

/** Цель дня по умолчанию, XP. */
const val DEFAULT_GOAL = 30

/** Значение перечисления из настроек; нет или повреждено — default. */
inline fun <reified T : Enum<T>> SharedPreferences.getEnum(key: String, default: T): T {
    val saved = getString(key, null)
    return enumValues<T>().firstOrNull { it.name == saved } ?: default
}

/** Записывает строку (или удаляет ключ при null). */
fun SharedPreferences.putPref(key: String, value: String?) {
    edit().putString(key, value).apply()
}
