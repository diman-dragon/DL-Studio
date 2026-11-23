package com.dlstudio.app.speech

/** Пол голоса. Android не сообщает его прямо, поэтому он берётся из имени голоса, из признаков движка и из выбора пользователя. */
object Gender {
    const val ANY = 0
    const val F = 1
    const val M = 2

    /** По служебному имени и признакам голоса: «female» — женский, «male» — мужской, иначе неизвестно. */
    fun of(name: String, features: Set<String> = emptySet()): Int {
        val all = (listOf(name) + features).joinToString(" ").lowercase()
        return when {
            "female" in all -> F
            MALE.containsMatchIn(all) -> M
            else -> ANY
        }
    }

    private val MALE = Regex("(?<![a-z])male")
}

/** Роль в диалоге: пол (`Gender`) и номер голоса внутри пола (0 — первый, 1 — второй…). Без пола — просто «другой голос». */
data class Role(val gender: Int = Gender.ANY, val n: Int = 0)

/** Установленный голос движка как кандидат на роль: пол, нужна ли сеть, совпадает ли регион, качество. */
data class Cand(val name: String, val gender: Int, val network: Boolean, val sameRegion: Boolean, val quality: Int)

/** Что произнести: служебное имя голоса (null — голос по умолчанию) и высота тона. */
data class Cast(val voice: String?, val pitch: Float = 1f)

/**
 * Подбор голоса под роль. Роль с полом получает голос этого пола: сначала выбранный в настройках, потом основной (если он того же пола),
 * затем лучшие по качеству. Если голосов этого пола нет, играет основной голос со сдвигом тона — мужская реплика не прозвучит ярко женским голосом.
 * Роль без пола: номер 0 — основной голос, остальные — другие установленные голоса, сначала противоположного пола.
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

    private fun rank(list: List<Cand>) = list.sortedWith(compareBy({ it.network }, { !it.sameRegion }, { -it.quality }, { it.name }))

    /** `explicit` — голоса, назначенные в настройках на пол (пол → имя); назначенный голос считается голосом этого пола. */
    fun pick(cands: List<Cand>, role: Role, base: String?, explicit: Map<Int, String>): Cast {
        val known = cands.map { c -> c.copy(gender = explicit.entries.firstOrNull { it.value == c.name }?.key ?: c.gender) }
        return if (role.gender == Gender.ANY) Cast(anyVoice(known, role.n, base)) else byGender(known, role, base, explicit)
    }

    private fun byGender(known: List<Cand>, role: Role, base: String?, explicit: Map<Int, String>): Cast {
        val ranked = rank(known.filter { it.gender == role.gender }).map { it.name }
        val first = listOfNotNull(explicit[role.gender], base).distinct().filter { it in ranked }
        val pool = first + ranked.filter { it !in first }
        val female = role.gender == Gender.F
        if (pool.isEmpty()) {
            val shifted = if (female) PITCH_F + STEP_F * role.n else PITCH_M - STEP_M * role.n
            return Cast(base, shifted.coerceIn(PITCH_MIN, PITCH_MAX))
        }
        val round = role.n / pool.size
        val pitch = if (female) 1f + ROUND_F * round else 1f - ROUND_M * round
        return Cast(pool[role.n % pool.size], pitch.coerceIn(PITCH_MIN, PITCH_MAX))
    }

    private fun anyVoice(known: List<Cand>, n: Int, base: String?): String? {
        if (n <= 0) return base
        val baseGender = known.firstOrNull { it.name == base }?.gender ?: Gender.ANY
        val others = known.filter { it.name != base }
        val ordered = rank(others.filter { baseGender != Gender.ANY && it.gender != baseGender && it.gender != Gender.ANY }) + rank(others.filter { baseGender == Gender.ANY || it.gender == baseGender || it.gender == Gender.ANY })
        return if (ordered.isEmpty()) null else ordered[(n - 1) % ordered.size].name
    }
}
