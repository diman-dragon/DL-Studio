package com.dlstudio.app.data

import android.content.Context
import com.dlstudio.app.study.CardMode
import com.dlstudio.app.study.StudyOptions

/** Настройки учёбы в тех же SharedPreferences, что и `Prefs`: режимы карточек, проверка произношения, поздравления. */
class StudyPrefs(context: Context) {
    private val sp = context.getSharedPreferences("dlstudio", Context.MODE_PRIVATE)

    fun load(): StudyOptions {
        val saved = sp.getString("modes", null)?.split(',').orEmpty()
        val modes = CardMode.values().filter { it.name in saved }.toSet().ifEmpty { CardMode.values().toSet() }
        return StudyOptions(modes, sp.getBoolean("pronounce", true), sp.getBoolean("celebrate", true))
    }

    fun save(options: StudyOptions) {
        sp.edit().putString("modes", options.modes.joinToString(",") { it.name })
            .putBoolean("pronounce", options.pronounce).putBoolean("celebrate", options.celebrate).apply()
    }
}
