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
}
