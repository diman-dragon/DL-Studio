package com.dlstudio.app.speech

/** Постоянные персонажи учебных диалогов. ID используются в контенте, настройках голосов и анимации говорящего. */
data class CharacterProfile(
    val id: String,
    val name: String,
    val gender: Int,
    val voiceIndex: Int,
    val description: String
)

object Characters {
    val all = listOf(
        CharacterProfile("grace", "Miss Grace", Gender.F, 0, "Главный преподаватель: объясняет правила и ведёт урок"),
        CharacterProfile("leo", "Mr. Leo", Gender.M, 0, "Главный преподаватель: объясняет на примерах и поддерживает ученика"),
        CharacterProfile("beatrice", "Beatrice", Gender.F, 1, "Ученица: задаёт вопросы и участвует в диалогах"),
        CharacterProfile("john", "John", Gender.M, 1, "Ученик: отвечает, уточняет и участвует в диалогах"),
        CharacterProfile("narrator", "Narrator", Gender.ANY, 0, "Нейтральный голос инструкций")
    )
    /** Персонаж без отметки в тексте: фраза звучит голосом мисс Грейс. */
    const val DEFAULT = "grace"
    private val byId = all.associateBy { it.id }
    fun get(id: String?): CharacterProfile? = id?.let(byId::get)
    fun name(id: String?): String? = get(id)?.name

    /** Профиль говорящего: null (нет отметки) — мисс Грейс. */
    fun of(id: String?): CharacterProfile = get(id) ?: byId.getValue(DEFAULT)
}
