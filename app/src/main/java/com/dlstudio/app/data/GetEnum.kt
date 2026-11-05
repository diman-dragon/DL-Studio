package com.dlstudio.app.data

import android.content.SharedPreferences

/** Значение перечисления из настроек; нет или повреждено — `default`. */
inline fun <reified T : Enum<T>> SharedPreferences.getEnum(key: String, default: T): T {
    val saved = getString(key, null)
    return enumValues<T>().firstOrNull { it.name == saved } ?: default
}
