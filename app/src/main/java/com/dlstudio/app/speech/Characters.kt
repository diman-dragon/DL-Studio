package com.dlstudio.app.speech

/** Роль персонажа в курсе: преподаватель ведёт урок, ученик участвует в диалогах, диктор читает инструкции. */
enum class CharRole { TEACHER, STUDENT, NARRATOR }

/**
 * Постоянный персонаж учебных диалогов: ID (его пишут в уроках как `~id `), имя для озвучки и приветствия, пол голоса, роль и цвет круга за портретом.
 * Внешность — портреты `ui/components/PortraitRes.kt`; подписи, роль и характер на языке интерфейса — `Strings.character*`.
 * Голос: `tone` и `pace` — высота и скорость при собственном голосе; `fallbackPitch` — высота, когда подходящего голоса нет (общий или голос системы по умолчанию),
 * чтобы персонажи всё равно звучали по-разному.
 */
data class CharacterProfile(
    val id: String, val name: String, val gender: Int, val role: CharRole, val tint: Long,
    val tone: Float, val pace: Float, val fallbackPitch: Float
)

/** Единый список персонажей. Порядок важен: раньше стоящий первым получает лучший голос (преподаватели — лучшие, затем ученики, диктор последним). */
object Characters {
    val all = listOf(
        CharacterProfile("grace", "Miss Grace", Gender.F, CharRole.TEACHER, 0xFFB8A6FF, 1.00f, 0.95f, 1.25f),
        CharacterProfile("leo", "Mr. Leo", Gender.M, CharRole.TEACHER, 0xFFFFC857, 1.00f, 1.00f, 0.80f),
        CharacterProfile("beatrice", "Beatrice", Gender.F, CharRole.STUDENT, 0xFF7BDDEB, 1.06f, 1.05f, 1.45f),
        CharacterProfile("john", "John", Gender.M, CharRole.STUDENT, 0xFF9287FF, 0.97f, 1.04f, 0.68f),
        CharacterProfile("narrator", "Narrator", Gender.ANY, CharRole.NARRATOR, 0xFFE8D9A0, 1.00f, 0.92f, 0.88f)
    )

    /** Персонаж без отметки в тексте: фраза звучит голосом мисс Грейс. */
    const val DEFAULT = "grace"
    private val byId = all.associateBy { it.id }
    fun get(id: String?): CharacterProfile? = id?.let(byId::get)

    /** Профиль говорящего: null (нет отметки) — мисс Грейс. */
    fun of(id: String?): CharacterProfile = get(id) ?: byId.getValue(DEFAULT)

    /** Преподаватели в порядке списка. */
    val teachers = all.filter { it.role == CharRole.TEACHER }
}
