package com.dlstudio.app.ui.components

import android.speech.tts.Voice
import java.util.Locale

/**
 * Человеческие имена для системных голосов TTS: внутри списка не повторяются, страна и язык — на языке интерфейса.
 * Пол берётся только из названия голоса («female» / «male»); если там его нет, имя нейтральное (Саша, Алекс), чтобы женскому голосу не досталось «Томас».
 */
object VoiceName {
    private val ruFemale = listOf("Алёна", "Анна", "Екатерина", "Мария", "Дарья", "Ольга", "Ирина", "Наталья", "Софья", "Полина", "Татьяна", "Елена")
    private val ruMale = listOf("Игорь", "Александр", "Дмитрий", "Максим", "Сергей", "Артём", "Андрей", "Михаил", "Николай", "Павел", "Иван", "Владимир")
    private val enFemale = listOf("Оливия", "Эмма", "Джессика", "София", "Алиса", "Виктория", "Мия", "Эмили", "Грейс", "Лилия", "Клэр", "Ханна")
    private val enMale = listOf("Джеймс", "Оливер", "Уильям", "Майкл", "Джон", "Дэвид", "Дэниел", "Томас", "Генри", "Люк", "Адам", "Эндрю")
    private val neutral = listOf("Алекс", "Сэм", "Робин", "Джордан", "Тейлор", "Кэмерон", "Райли", "Квинн", "Саша", "Женя", "Валя", "Слава")

    private fun pool(language: String, name: String): List<String> {
        val lower = name.lowercase()
        val ru = language.lowercase() == "ru"
        return when {
            lower.contains("female") -> if (ru) ruFemale else enFemale
            lower.contains("male") -> if (ru) ruMale else enMale
            else -> neutral
        }
    }

    private fun country(loc: Locale, ui: Locale): String = loc.getDisplayCountry(ui).ifBlank { loc.getDisplayLanguage(ui) }.let { if (it.isBlank()) "" else " ($it)" }

    /** Имена для всех голосов списка: у разных голосов с одной страной имена разные (при нехватке имён — с номером). */
    fun labels(voices: List<Voice>, ui: Locale): Map<String, String> {
        val taken = HashSet<String>()
        return voices.sortedBy { it.name }.associate { v ->
            val loc = v.locale ?: Locale.getDefault()
            val names = pool(loc.language, v.name)
            val start = Math.floorMod(v.name.hashCode(), names.size)
            v.name to generateSequence(0) { it + 1 }.map { k ->
                val n = names[(start + k) % names.size]
                (if (k < names.size) n else "$n ${k / names.size + 1}") + country(loc, ui)
            }.first { taken.add(it) }
        }
    }

    /** Название языка голоса на языке интерфейса, с заглавной буквы. */
    fun language(voice: Voice, ui: Locale): String = voice.locale.getDisplayName(ui).replaceFirstChar { it.titlecase(ui) }

    /** Имя для голоса, выбранного раньше, чем имя сохранилось: язык и страна берутся из названия голоса («en-us-x-…»). */
    fun fallback(name: String, ui: Locale): String {
        val parts = name.split('-', '_')
        val loc = Locale(parts[0], parts.getOrNull(1)?.takeIf { it.length == 2 }.orEmpty())
        val names = pool(loc.language, name)
        return names[Math.floorMod(name.hashCode(), names.size)] + country(loc, ui)
    }
}
