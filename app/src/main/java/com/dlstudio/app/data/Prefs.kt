package com.dlstudio.app.data

import android.content.Context
import android.content.SharedPreferences
import com.dlstudio.app.settings.AccentColor
import com.dlstudio.app.settings.AppLang
import com.dlstudio.app.settings.ThemeMode
import java.util.Locale
import org.json.JSONObject

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

    /** Выбранные голоса озвучки: язык («en») → служебное имя голоса встроенного движка Android. Нет записи — голос по умолчанию. */
    var voices: Map<String, String>
        get() = runCatching { JSONObject(sp.getString("voices", "{}").orEmpty()).let { o -> o.keys().asSequence().associateWith { o.getString(it) } } }.getOrDefault(emptyMap())
        set(value) = sp.putPref("voices", JSONObject(value).toString())

    /** Голоса, которые приложение подобрало персонажам само и закрепило («en:grace» → голос): персонаж не меняет голос, когда в системе появляются новые. */
    var castAuto: Map<String, String>
        get() = runCatching { JSONObject(sp.getString("castAuto", "{}").orEmpty()).let { o -> o.keys().asSequence().associateWith { o.getString(it) } } }.getOrDefault(emptyMap())
        set(value) = sp.putPref("castAuto", JSONObject(value).toString())

    /** Персонажи, с которыми ученик уже познакомился (первое появление в уроке показано). */
    var met: Set<String>
        get() = sp.getStringSet("met", emptySet()).orEmpty().toSet()
        set(value) { sp.edit().putStringSet("met", value).apply() }

    /** Курсы, в начале которых героев уже представили. */
    var introduced: Set<String>
        get() = sp.getStringSet("introduced", emptySet()).orEmpty().toSet()
        set(value) { sp.edit().putStringSet("introduced", value).apply() }

    /** Скорость озвучки: 1 — обычная, меньше — медленнее. */
    var speechRate: Float
        get() = sp.getFloat("speechRate", 1f)
        set(value) { sp.edit().putFloat("speechRate", value).apply() }

    /** Последняя открытая тема (`id`): карта при запуске раскрывает её урок и встаёт на неё. */
    var lastTopic: String
        get() = sp.getString("lastTopic", "").orEmpty()
        set(value) = sp.putPref("lastTopic", value)

    /** Отчёт о последнем падении (пусто — падения не было или его уже показали). */
    var crashReport: String?
        get() = sp.getString("crash", null)
        set(value) = sp.putPref("crash", value)

    /** Сохраняет отчёт синхронно: процесс уже падает, отложенная запись не успеет. */
    fun saveCrashNow(report: String) { sp.edit().putString("crash", report).commit() }

    var placesJson: String?
        get() = sp.getString("places", null)
        set(value) = sp.putPref("places", value)

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
