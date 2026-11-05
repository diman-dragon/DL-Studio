package com.dlstudio.app.data

import android.content.SharedPreferences

/** Записывает строку (или удаляет ключ при null). */
fun SharedPreferences.putPref(key: String, value: String?) {
    edit().putString(key, value).apply()
}
