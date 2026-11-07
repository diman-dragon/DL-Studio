package com.dlstudio.app.ui.components

import android.speech.tts.Voice
import java.util.Locale
import kotlin.math.abs

/** Человеческие имена для системных голосов TTS (Алёна, Игорь, Оливия и т.д.). */
object VoiceName {
    private val ruFemale = listOf("Алёна", "Анна", "Екатерина", "Мария", "Дарья", "Ольга")
    private val ruMale = listOf("Игорь", "Александр", "Дмитрий", "Максим", "Сергей", "Артём")
    private val enFemale = listOf("Оливия", "Эмма", "Джессика", "София", "Алиса", "Виктория")
    private val enMale = listOf("Джеймс", "Оливер", "Уильям", "Майкл", "Джон", "Дэвид")

    fun label(voice: Voice): String {
        val loc = voice.locale ?: Locale.getDefault()
        val lang = loc.language.lowercase()
        val isRu = lang == "ru"
        val lower = voice.name.lowercase()
        val isFem = lower.contains("fem") || lower.contains("wom") || lower.contains("eva")
        val pool = when {
            isRu && isFem -> ruFemale
            isRu -> ruMale
            isFem -> enFemale
            else -> enMale
        }
        val name = pool[abs(voice.name.hashCode()) % pool.size]
        val country = loc.getDisplayCountry(loc).ifBlank { loc.getDisplayLanguage(loc) }
        return if (country.isNotBlank()) "$name ($country)" else name
    }
}
