package com.dlstudio.app.speech

/** Голос движка синтеза речи Android, как его отдаёт система: служебное имя, язык с регионом, качество, состояние и пол (`Gender`, по имени и признакам голоса; неизвестен — ANY). */
data class RawVoice(val name: String, val tag: String, val quality: Int, val network: Boolean, val installed: Boolean, val gender: Int = Gender.ANY)

/** Голос для списка в настройках: человеческое имя, регион и признаки. `name` — служебное имя для движка. */
data class VoiceChoice(val name: String, val title: String, val tag: String, val region: String, val high: Boolean, val network: Boolean, val installed: Boolean, val gender: Int = Gender.ANY)

/**
 * Служебные имена голосов («en-us-x-sfg#female_1-local») человеку ничего не говорят, поэтому каждому голосу даётся имя вроде «Emma» или «Алёна».
 * Имя выбирается по имени голоса, поэтому остаётся тем же при каждом запуске; одинаковых имён внутри языка не бывает.
 * Имя всегда соответствует полу: женским голосам — женские имена, мужским — мужские (Сергей не заговорит женским голосом); если пол неизвестен, имя нейтральное
 * («Алекс», «Саша»). Пол берётся из имени и признаков голоса, а голос, который пользователь назначил на «женский» или «мужской» в настройках, считается голосом этого пола.
 */
object VoiceNames {
    private val EN_F = listOf("Emma", "Olivia", "Ava", "Mia", "Chloe", "Grace", "Lily", "Ella", "Sophie", "Hannah", "Ruby", "Alice")
    private val EN_M = listOf("James", "Noah", "Liam", "Oliver", "Jack", "Henry", "Ethan", "Leo", "Harry", "George", "Oscar", "Daniel")
    private val RU_F = listOf("Алёна", "Мария", "Дарья", "Анна", "Полина", "Ольга", "Ксения", "Вера", "Нина", "Юлия")
    private val RU_M = listOf("Дмитрий", "Иван", "Максим", "Артём", "Сергей", "Михаил", "Павел", "Никита", "Андрей", "Фёдор")
    private val EN_N = listOf("Alex", "Sam", "Robin", "Taylor", "Jordan", "Casey", "Morgan", "Jamie", "Kai", "Quinn")
    private val RU_N = listOf("Саша", "Женя", "Валя", "Шура", "Слава", "Тима", "Лёля", "Дана", "Ярик", "Рита")
    private val ANY_N = listOf("Alex", "Sam", "Robin", "Kai", "Noa", "Dani", "Rene", "Sasha", "Luca", "Ari")
    private val ANY_F = listOf("Sofia", "Anna", "Julia", "Clara", "Eva", "Nora", "Elena", "Laura", "Marta", "Lena")
    private val ANY_M = listOf("Marco", "Lucas", "Max", "Adam", "Victor", "Hugo", "Felix", "Pablo", "Simon", "Rafael")

    private fun pool(lang: String, g: Int): List<String> = when (lang) {
        "en" -> when (g) { Gender.F -> EN_F; Gender.M -> EN_M; else -> EN_N }
        "ru" -> when (g) { Gender.F -> RU_F; Gender.M -> RU_M; else -> RU_N }
        else -> when (g) { Gender.F -> ANY_F; Gender.M -> ANY_M; else -> ANY_N }
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
     * Голоса языка `lang` (например «en») в порядке показа: скачанные выше, затем по качеству и имени.
     * Каждому голосу назначается имя; при нехватке имён в списке к имени добавляется номер.
     */
    fun choices(raw: List<RawVoice>, lang: String, ru: Boolean, chosen: Map<String, String> = emptyMap()): List<VoiceChoice> {
        val mine = raw.filter { language(it.tag) == lang }.sortedBy { it.name }
        val taken = HashSet<String>()
        val assigned = listOfNotNull(chosen["$lang:f"]?.let { it to Gender.F }, chosen["$lang:m"]?.let { it to Gender.M }).toMap()
        val named = mine.mapIndexed { index, v ->
            val g = assigned[v.name] ?: v.gender
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
