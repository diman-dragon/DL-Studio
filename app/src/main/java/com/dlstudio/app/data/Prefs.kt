package com.dlstudio.app.data

import android.content.Context
import com.dlstudio.app.settings.AccentColor
import com.dlstudio.app.settings.AppLang
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
        get() = sp.getString("speechVoiceLabel", null)
        set(value) = sp.putPref("speechVoiceLabel", value)

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
