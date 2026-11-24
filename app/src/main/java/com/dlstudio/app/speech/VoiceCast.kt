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

/** Роль в диалоге: пол (`Gender`) и номер голоса внутри пола (0 — первый, 1 — второй…). Роль без пола: первая — женщина, вторая — мужчина, дальше по очереди. */
data class Role(val gender: Int = Gender.ANY, val n: Int = 0) {
    /** Кто говорит: пол героя; у роли без пола чётные номера — учительница, нечётные — преподаватель. */
    fun speaker(): Int = if (gender != Gender.ANY) gender else if (n % 2 == 0) Gender.F else Gender.M

    /** Номер голоса внутри пола. */
    fun index(): Int = if (gender != Gender.ANY) n else n / 2
}

/** Установленный голос с известным полом как кандидат на роль: нужна ли сеть, совпадает ли регион, качество. */
data class Cand(val name: String, val gender: Int, val network: Boolean, val sameRegion: Boolean, val quality: Int)

/** Что произнести: служебное имя голоса (null — голос по умолчанию) и высота тона. */
data class Cast(val voice: String?, val pitch: Float = 1f)

/**
 * Подбор голоса под роль. Первая учительница и первый преподаватель — голоса, выбранные в настройках (`explicit`: пол → имя), иначе самые качественные
 * голоса своего пола. Следующие герои — следующие по качеству голоса того же пола. Если голосов этого пола нет, играет голос по умолчанию
 * со сдвигом тона: мужская реплика не прозвучит ярко женским голосом.
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

    fun pick(cands: List<Cand>, role: Role, explicit: Map<Int, String>): Cast {
        val gender = role.speaker()
        val n = role.index()
        val ranked = rank(cands.filter { it.gender == gender }).map { it.name }
        val first = listOfNotNull(explicit[gender]).filter { it in ranked }
        val pool = first + ranked.filter { it !in first }
        val female = gender == Gender.F
        if (pool.isEmpty()) {
            val shifted = if (female) PITCH_F + STEP_F * n else PITCH_M - STEP_M * n
            return Cast(null, shifted.coerceIn(PITCH_MIN, PITCH_MAX))
        }
        val round = n / pool.size
        val pitch = if (female) 1f + ROUND_F * round else 1f - ROUND_M * round
        return Cast(pool[n % pool.size], pitch.coerceIn(PITCH_MIN, PITCH_MAX))
    }
}
