package com.dlstudio.app.ui.components

import android.speech.tts.Voice
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dlstudio.app.ui.theme.LocalStrings
import java.util.Locale

/** Выбор голоса с понятными человеческими именами («Алёна», «Игорь» и т.д.). */
@Composable
fun VoicePicker(voices: List<Voice>, selected: String?, ui: Locale, onPreview: (Voice, String) -> Unit, onSelect: (Voice) -> Unit, onClose: () -> Unit) {
    val s = LocalStrings.current; val colors = MaterialTheme.colorScheme
    val names = remember(voices, ui) { VoiceName.labels(voices, ui) }
    DlModal.Shell(s.speechVoice, onClose) {
        Column(Modifier.heightIn(max = 390.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (voices.isEmpty()) Text(s.voicePackagesEmpty, color = colors.onSurfaceVariant)
            voices.sortedWith(compareBy({ it.locale.displayName }, { it.name })).forEach { voice ->
                val language = VoiceName.language(voice, ui)
                val on = voice.name == selected
                Surface(
                    Modifier.fillMaxWidth().clickable { onSelect(voice) }, RoundedCornerShape(16.dp),
                    if (on) colors.primaryContainer else colors.surfaceVariant,
                    border = BorderStroke(1.dp, if (on) colors.primary else colors.outlineVariant)
                ) {
                    Row(Modifier.padding(14.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(names[voice.name] ?: VoiceName.fallback(voice.name, ui), style = MaterialTheme.typography.titleSmall)
                            Text(language, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                        }
                        DlModal.Action("▶", { onPreview(voice, if (voice.locale.language == "ru") s.voicePreview else "Hello! This is how I sound.") })
                    }
                }
            }
        }
    }
}

/**
 * Человеческие имена для системных голосов TTS: внутри списка не повторяются, страна и язык — на языке интерфейса.
 * Пол берётся только из названия голоса («female» / «male»); если там его нет, имя нейтральное (Саша, Алекс), чтобы женскому голосу не досталось «Томас».
 */
object VoiceName {
    private fun list(text: String) = text.split(' ')
    private val cyrillic = mapOf(
        "ru-f" to list("Алёна Анна Екатерина Мария Дарья Ольга Ирина Наталья Софья Полина Татьяна Елена"),
        "ru-m" to list("Игорь Александр Дмитрий Максим Сергей Артём Андрей Михаил Николай Павел Иван Владимир"),
        "en-f" to list("Оливия Эмма Джессика София Алиса Виктория Мия Эмили Грейс Лилия Клэр Ханна"),
        "en-m" to list("Джеймс Оливер Уильям Майкл Джон Дэвид Дэниел Томас Генри Люк Адам Эндрю"),
        "any" to list("Алекс Сэм Робин Джордан Тейлор Кэмерон Райли Квинн Саша Женя Валя Слава")
    )
    private val latin = mapOf(
        "ru-f" to list("Alyona Anna Ekaterina Maria Daria Olga Irina Natalia Sofia Polina Tatiana Elena"),
        "ru-m" to list("Igor Alexander Dmitry Maxim Sergey Artyom Andrey Mikhail Nikolay Pavel Ivan Vladimir"),
        "en-f" to list("Olivia Emma Jessica Sophia Alice Victoria Mia Emily Grace Lily Claire Hannah"),
        "en-m" to list("James Oliver William Michael John David Daniel Thomas Henry Luke Adam Andrew"),
        "any" to list("Alex Sam Robin Jordan Taylor Cameron Riley Quinn Sasha Jamie Charlie Morgan")
    )

    /** Имена по языку интерфейса (кириллица для русского, латиница для остальных), языку голоса и полу из названия голоса. */
    private fun pool(language: String, name: String, ui: Locale): List<String> {
        val lower = name.lowercase()
        val lang = if (language.lowercase() == "ru") "ru" else "en"
        val key = when {
            lower.contains("female") -> "$lang-f"
            lower.contains("male") -> "$lang-m"
            else -> "any"
        }
        return (if (ui.language == "ru") cyrillic else latin).getValue(key)
    }

    private fun country(loc: Locale, ui: Locale): String = loc.getDisplayCountry(ui).ifBlank { loc.getDisplayLanguage(ui) }.let { if (it.isBlank()) "" else " ($it)" }

    /** Имена для всех голосов списка: у разных голосов с одной страной имена разные (при нехватке имён — с номером). */
    fun labels(voices: List<Voice>, ui: Locale): Map<String, String> {
        val taken = HashSet<String>()
        return voices.sortedBy { it.name }.associate { v ->
            val loc = v.locale ?: Locale.getDefault()
            val names = pool(loc.language, v.name, ui)
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
        val names = pool(loc.language, name, ui)
        return names[Math.floorMod(name.hashCode(), names.size)] + country(loc, ui)
    }
}
