package com.dlstudio.app.ui.components

import android.speech.tts.Voice
import java.util.Locale

/** Человеческие имена для системных голосов TTS (Алёна, Игорь, Оливия…): внутри одного списка не повторяются. */
object VoiceName {
    private val ruFemale = listOf("Алёна", "Анна", "Екатерина", "Мария", "Дарья", "Ольга", "Ирина", "Наталья", "Софья", "Полина", "Татьяна", "Елена")
    private val ruMale = listOf("Игорь", "Александр", "Дмитрий", "Максим", "Сергей", "Артём", "Андрей", "Михаил", "Николай", "Павел", "Иван", "Владимир")
    private val enFemale = listOf("Оливия", "Эмма", "Джессика", "София", "Алиса", "Виктория", "Мия", "Эмили", "Грейс", "Лилия", "Клэр", "Ханна")
    private val enMale = listOf("Джеймс", "Оливер", "Уильям", "Майкл", "Джон", "Дэвид", "Дэниел", "Томас", "Генри", "Люк", "Адам", "Эндрю")

    private fun pool(language: String, name: String): List<String> {
        val lower = name.lowercase()
        val female = lower.contains("fem") || lower.contains("wom") || lower.contains("eva")
        return if (language.lowercase() == "ru") (if (female) ruFemale else ruMale) else (if (female) enFemale else enMale)
    }

    private fun country(loc: Locale): String = loc.getDisplayCountry(loc).ifBlank { loc.getDisplayLanguage(loc) }.let { if (it.isBlank()) "" else " ($it)" }

    /** Имена для всех голосов списка: у разных голосов с одной страной имена разные (при нехватке имён — с номером). */
    fun labels(voices: List<Voice>): Map<String, String> {
        val taken = HashSet<String>()
        return voices.sortedBy { it.name }.associate { v ->
            val loc = v.locale ?: Locale.getDefault()
            val names = pool(loc.language, v.name)
            val start = Math.floorMod(v.name.hashCode(), names.size)
            v.name to generateSequence(0) { it + 1 }.map { k ->
                val n = names[(start + k) % names.size]
                (if (k < names.size) n else "$n ${k / names.size + 1}") + country(loc)
            }.first { taken.add(it) }
        }
    }

    /** Имя для голоса, выбранного раньше, чем имя сохранилось: язык и страна берутся из названия голоса («en-us-x-…»). */
    fun fallback(name: String): String {
        val parts = name.split('-', '_')
        val loc = Locale(parts[0], parts.getOrNull(1)?.takeIf { it.length == 2 }.orEmpty())
        val names = pool(loc.language, name)
        return names[Math.floorMod(name.hashCode(), names.size)] + country(loc)
    }
}
