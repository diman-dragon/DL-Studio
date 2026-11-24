package com.dlstudio.app.speech

import java.util.Locale

/**
 * Пол голоса. Android не сообщает его в объекте Voice, поэтому пол берётся только из явных признаков: слов `female` / `male` в названии голоса
 * и из известных идентификаторов голосов Google (`en-us-x-sfg-local` → «sfg»). Голос, у которого пол явно не указан, в приложении не используется:
 * лучше сдвинуть тон голоса по умолчанию, чем женский персонаж заговорит мужским голосом.
 */
object Gender {
    const val ANY = 0
    const val F = 1
    const val M = 2

    /** Идентификаторы голосов Google с известным полом. Неоднозначные (iom, rjs — у разных сборок разный пол) не включены. */
    private val FEMALE = setOf("dfc", "ruf", "sfg", "iob", "tpc", "tpf", "gba", "gbc", "fis", "itf", "ita", "itb", "srf")
    private val MALE = setOf("dmc", "rud", "iol", "tpd", "gbb", "gbd", "itc", "itd")
    private val MALE_WORD = Regex("(?<![a-z])male")

    /** По служебному имени и признакам голоса; ANY — пол не указан. */
    fun of(name: String, features: Set<String> = emptySet()): Int {
        val all = (listOf(name) + features).joinToString(" ").lowercase()
        val parts = name.lowercase().split("-")
        val code = parts.getOrNull(3)?.substringBefore('#')?.takeIf { parts.getOrNull(2) == "x" }
        return when {
            "female" in all || code in FEMALE -> F
            MALE_WORD.containsMatchIn(all) || code in MALE -> M
            else -> ANY
        }
    }
}

/** Установленный голос с известным полом как кандидат на роль: нужна ли сеть, совпадает ли регион, качество. */
data class Cand(val name: String, val gender: Int, val network: Boolean, val sameRegion: Boolean, val quality: Int)

/** Как персонажу достался голос: выбран в настройках, собственный (подобран и закреплён), общий с другим персонажем (тон и скорость разведены), голос системы по умолчанию. */
enum class How { CHOSEN, OWN, SHARED, DEFAULT }

/** Что произнести: служебное имя голоса (null — голос по умолчанию), высота тона, множитель скорости речи и способ, которым достался голос. */
data class Cast(val voice: String?, val pitch: Float = 1f, val rate: Float = 1f, val how: How = How.OWN)

/**
 * Распределение голосов между всеми персонажами сразу, чтобы два говорящих не получили один голос без нужды.
 * Порядок: 1) голос, выбранный в настройках; 2) закреплённый прежде (`sticky`) — персонаж не меняет голос, когда в системе появляются новые;
 * 3) лучший свободный голос его пола (качество, офлайн, регион, имя); 4) если свободных нет — наименее занятый голос пола со сдвигом тона и скорости;
 * 5) если голосов пола нет совсем — голос по умолчанию со своим тоном и скоростью персонажа. Диктору (пол не важен) подходит любой голос.
 */
object VoiceCast {
    private const val STEP_F = 0.15f
    private const val STEP_M = 0.12f
    private const val PITCH_MIN = 0.5f
    private const val PITCH_MAX = 2f

    /** Лучшие первыми: качество, потом офлайн, потом совпадение региона, потом имя. */
    fun rank(list: List<Cand>) = list.sortedWith(compareBy({ -it.quality }, { it.network }, { !it.sameRegion }, { it.name }))

    /** Скачанные голоса языка темы `tag` с известным полом. */
    fun cands(raw: List<RawVoice>, tag: String): List<Cand> {
        val lang = VoiceNames.language(tag)
        val country = Locale.forLanguageTag(tag).country
        return raw.filter { VoiceNames.language(it.tag) == lang && it.installed && it.gender != Gender.ANY }
            .map { Cand(it.name, it.gender, it.network, Locale.forLanguageTag(it.tag).country == country, it.quality) }
    }

    /** Распределение для языка темы `tag`; `chosen` и `sticky` — карты «язык:персонаж» → голос (как в настройках). */
    fun plan(raw: List<RawVoice>, tag: String, chosen: Map<String, String>, sticky: Map<String, String>): Map<String, Cast> {
        val lang = VoiceNames.language(tag)
        fun mine(m: Map<String, String>) = Characters.all.mapNotNull { c -> m["$lang:${c.id}"]?.let { c.id to it } }.toMap()
        return assign(cands(raw, tag), mine(chosen), mine(sticky))
    }

    /** Распределение по ID персонажа; `chosen` и `sticky` — ID персонажа → голос. */
    fun assign(cands: List<Cand>, chosen: Map<String, String>, sticky: Map<String, String>): Map<String, Cast> {
        val out = LinkedHashMap<String, Cast>()
        val users = HashMap<String, Int>()
        fun fits(who: CharacterProfile, voice: String) = cands.any { it.name == voice && (who.gender == Gender.ANY || it.gender == who.gender) }
        fun give(who: CharacterProfile, voice: String, how: How) { out[who.id] = Cast(voice, who.tone, who.pace, how); users.merge(voice, 1, Int::plus) }
        for (who in Characters.all) chosen[who.id]?.takeIf { fits(who, it) }?.let { give(who, it, How.CHOSEN) }
        for (who in Characters.all) if (who.id !in out) sticky[who.id]?.takeIf { fits(who, it) && it !in users }?.let { give(who, it, How.OWN) }
        for (who in Characters.all) {
            if (who.id in out) continue
            val ranked = rank(if (who.gender == Gender.ANY) cands else cands.filter { it.gender == who.gender }).map { it.name }
            val free = ranked.firstOrNull { it !in users }
            when {
                free != null -> give(who, free, How.OWN)
                ranked.isNotEmpty() -> {
                    val voice = ranked.minByOrNull { users[it] ?: 0 }!!
                    out[who.id] = Cast(voice, shared(who, users[voice] ?: 1), who.pace, How.SHARED)
                    users.merge(voice, 1, Int::plus)
                }
                else -> out[who.id] = Cast(null, who.fallbackPitch.coerceIn(PITCH_MIN, PITCH_MAX), who.pace, How.DEFAULT)
            }
        }
        return out
    }

    /** Тон на голосе, которым уже говорят `others` персонажей: женские выше, мужские ниже, диктор чуть ниже обычного. */
    private fun shared(who: CharacterProfile, others: Int): Float {
        val n = others.coerceAtLeast(1)
        val pitch = when (who.gender) { Gender.F -> 1f + STEP_F * n; Gender.M -> 1f - STEP_M * n; else -> who.fallbackPitch }
        return pitch.coerceIn(PITCH_MIN, PITCH_MAX)
    }
}
