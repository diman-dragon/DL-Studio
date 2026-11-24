package com.dlstudio.app.speech

/**
 * Пол голоса. Android не сообщает его в объекте Voice, поэтому пол берётся только из явных признаков: слов `female` / `male` в названии голоса
 * и из известных идентификаторов голосов Google (`en-us-x-sfg-local` → «sfg»). Голос, у которого пол явно не указан, в приложении не используется.
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

/** Что произнести: служебное имя голоса (null — голос по умолчанию) и высота тона. */
data class Cast(val voice: String?, val pitch: Float = 1f)

/**
 * Подбор голоса персонажу. Сначала голос, выбранный для него в настройках, затем самый качественный установленный голос его пола
 * (второй персонаж того же пола берёт второй по качеству). Если голосов нужного пола нет, играет голос по умолчанию со сдвигом тона.
 */
object VoiceCast {
    private const val PITCH_F = 1.3f
    private const val PITCH_M = 0.75f
    private const val STEP_F = 0.1f
    private const val STEP_M = 0.05f
    private const val ROUND_F = 0.15f
    private const val ROUND_M = 0.12f
    private const val PITCH_MIN = 0.5f
    private const val PITCH_MAX = 2f

    /** Лучшие первыми: качество, потом офлайн, потом совпадение региона, потом имя. */
    fun rank(list: List<Cand>) = list.sortedWith(compareBy({ -it.quality }, { it.network }, { !it.sameRegion }, { it.name }))

    fun pick(cands: List<Cand>, who: CharacterProfile, chosen: String?): Cast {
        val ranked = rank(if (who.gender == Gender.ANY) cands else cands.filter { it.gender == who.gender }).map { it.name }
        if (chosen != null && chosen in ranked) return Cast(chosen)
        val n = who.voiceIndex
        if (ranked.isEmpty()) {
            val shifted = when (who.gender) { Gender.F -> PITCH_F + STEP_F * n; Gender.M -> PITCH_M - STEP_M * n; else -> 1f }
            return Cast(null, shifted.coerceIn(PITCH_MIN, PITCH_MAX))
        }
        val round = n / ranked.size
        val pitch = when (who.gender) { Gender.F -> 1f + ROUND_F * round; Gender.M -> 1f - ROUND_M * round; else -> 1f }
        return Cast(ranked[n % ranked.size], pitch.coerceIn(PITCH_MIN, PITCH_MAX))
    }
}
