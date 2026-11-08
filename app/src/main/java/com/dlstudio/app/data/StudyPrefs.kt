package com.dlstudio.app.data

import android.content.Context
import com.dlstudio.app.study.CardMode
import com.dlstudio.app.study.StudyOptions

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
        val modes = CardMode.values().filter { it.name !in off }.toSet().ifEmpty { CardMode.values().toSet() }
        return StudyOptions(modes, sp.getBoolean("pronounce", true), sp.getBoolean("celebrate", true), sp.getInt("flipShare", StudyOptions.DEFAULT_FLIP_SHARE))
    }

    fun save(options: StudyOptions) {
        sp.edit().putString("modesOff", CardMode.values().filter { it !in options.modes }.joinToString(",") { it.name })
            .putBoolean("pronounce", options.pronounce).putBoolean("celebrate", options.celebrate).putInt("flipShare", options.flipShare).apply()
    }

    private companion object { val LEGACY = listOf("CHOICE_FWD", "CHOICE_REV", "LISTEN", "WRITE_FWD", "WRITE_REV") }
}
