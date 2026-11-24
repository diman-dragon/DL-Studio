package com.dlstudio.app.speech

/** Голос движка синтеза речи Android, как его отдаёт система: служебное имя, язык с регионом, качество, состояние и пол (`Gender`, по имени и признакам голоса; неизвестен — ANY). */
data class RawVoice(val name: String, val tag: String, val quality: Int, val network: Boolean, val installed: Boolean, val gender: Int = Gender.ANY)

/** Голос для списка в настройках: человеческое имя, регион и признаки. `name` — служебное имя для движка. */
data class VoiceChoice(val name: String, val title: String, val tag: String, val region: String, val high: Boolean, val network: Boolean, val installed: Boolean, val gender: Int = Gender.ANY)

/**
 * Служебные имена голосов («en-us-x-sfg-local») человеку ничего не говорят, поэтому каждому голосу даётся имя вроде «Emma» или «Алёна».
 * В список попадают только голоса с явно известным полом (см. Gender), имя всегда соответствует полу: женским голосам — женские имена,
 * мужским — мужские (Сергей не заговорит женским голосом). Имя выбирается по имени голоса, поэтому остаётся тем же при каждом запуске;
 * одинаковых имён внутри языка не бывает.
 */
object VoiceNames {
    private val EN_F = listOf("Emma", "Olivia", "Ava", "Mia", "Chloe", "Grace", "Lily", "Ella", "Sophie", "Hannah", "Ruby", "Alice")
    private val EN_M = listOf("James", "Noah", "Liam", "Oliver", "Jack", "Henry", "Ethan", "Leo", "Harry", "George", "Oscar", "Daniel")
    private val RU_F = listOf("Алёна", "Мария", "Дарья", "Анна", "Полина", "Ольга", "Ксения", "Вера", "Нина", "Юлия")
    private val RU_M = listOf("Дмитрий", "Иван", "Максим", "Артём", "Сергей", "Михаил", "Павел", "Никита", "Андрей", "Фёдор")
    private val ANY_F = listOf("Sofia", "Anna", "Julia", "Clara", "Eva", "Nora", "Elena", "Laura", "Marta", "Lena")
    private val ANY_M = listOf("Marco", "Lucas", "Max", "Adam", "Victor", "Hugo", "Felix", "Pablo", "Simon", "Rafael")

    private fun pool(lang: String, g: Int): List<String> = when (lang) {
        "en" -> if (g == Gender.F) EN_F else EN_M
        "ru" -> if (g == Gender.F) RU_F else RU_M
        else -> if (g == Gender.F) ANY_F else ANY_M
    }

    private fun stableHash(s: String): Int = s.fold(7) { h, c -> (h * 31 + c.code) and 0x7fffffff }

    fun language(tag: String): String = tag.replace('_', '-').substringBefore('-').lowercase()

    private fun region(tag: String, ru: Boolean): String {
        val code = tag.replace('_', '-').substringAfter('-', "").uppercase()
        val known = mapOf(
            "US" to ("США" to "USA"), "GB" to ("Британия" to "Britain"), "AU" to ("Австралия" to "Australia"), "CA" to ("Канада" to "Canada"),
            "IN" to ("Индия" to "India"), "IE" to ("Ирландия" to "Ireland"), "ZA" to ("ЮАР" to "South Africa"), "NZ" to ("Новая Зеландия" to "New Zealand"),
            "RU" to ("Россия" to "Russia")
        )
        val name = known[code]
        return if (name == null) code else if (ru) name.first else name.second
    }

    /**
     * Голоса языка `lang` (например «en») с известным полом в порядке показа: скачанные выше, затем по качеству и имени.
     * Каждому голосу назначается имя; при нехватке имён в списке к имени добавляется номер.
     */
    fun choices(raw: List<RawVoice>, lang: String, ru: Boolean): List<VoiceChoice> {
        val mine = raw.filter { language(it.tag) == lang && it.gender != Gender.ANY }.sortedBy { it.name }
        val taken = HashSet<String>()
        val named = mine.mapIndexed { index, v ->
            val g = v.gender
            val names = pool(lang, g)
            val start = stableHash(v.name) % names.size
            val free = (names.indices).map { names[(start + it) % names.size] }.firstOrNull { it !in taken }
            val title = free ?: (names[start] + " " + (index + 1))
            taken.add(title)
            VoiceChoice(v.name, title, v.tag, region(v.tag, ru), v.quality >= QUALITY_HIGH, v.network, v.installed, g)
        }
        return named.sortedWith(compareBy({ !it.installed }, { !it.high }, { it.tag }, { it.title }))
    }

    /** Качество «высокое» у голосов Android с уровнем от QUALITY_HIGH (400). */
    private const val QUALITY_HIGH = 400
}
