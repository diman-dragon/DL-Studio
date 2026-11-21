package com.dlstudio.app.learning

/** Stable domain model for the next learning architecture. */
enum class CourseStructure { LINE, GRAPH }

enum class TopicStatus { DRAFT, ACTIVE, COMPLETED, ARCHIVED }

enum class MaterialType {
    THEORY, FLASHCARD, WORD, PHRASE, TRANSLATION, GRAMMAR, EXAMPLE,
    IMAGE, AUDIO, PRONUNCIATION, EXERCISE, TEST, TASK, VIDEO, RESOURCE
}

data class Subject(
    val id: String,
    val name: String,
    val description: String = "",
    val metadata: Map<String, String> = emptyMap()
)

data class Course(
    val id: String,
    val subjectId: String,
    val name: String,
    val description: String = "",
    val structure: CourseStructure = CourseStructure.LINE,
    val metadata: Map<String, String> = emptyMap()
)

data class Topic(
    val id: String,
    val courseId: String,
    val name: String,
    val description: String = "",
    val order: Int = 0,
    val status: TopicStatus = TopicStatus.DRAFT,
    val dependencies: List<String> = emptyList(),
    val metadata: Map<String, String> = emptyMap()
)

data class LearningMaterial(
    val id: String,
    val topicId: String,
    val type: MaterialType,
    val title: String,
    val position: Int = 0,
    val payload: Map<String, String> = emptyMap()
)

/** A transparent record of something the learner actually did. */
data class ProgressEvent(
    val id: String,
    val topicId: String,
    val materialId: String? = null,
    val action: String,
    val occurredAt: Long,
    val durationMs: Long? = null,
    val metadata: Map<String, String> = emptyMap()
)

data class TopicProgress(
    val topicId: String,
    val startedAt: Long? = null,
    val completedMaterials: Set<String> = emptySet(),
    val completedExercises: Int = 0,
    val actionCount: Int = 0,
    val studyTimeMs: Long = 0,
    val completedAt: Long? = null,
    val lastActivityAt: Long? = null
)

data class MotivationTotals(
    val hp: Int = 0,
    val xp: Int = 0,
    val activeDays: Set<Long> = emptySet(),
    val achievements: Set<String> = emptySet()
)

/** Explicit event rewards; no knowledge estimate, schedule, or hidden coefficient is involved. */
object MotivationRules {
    const val BLOCK_HP = 5
    const val BLOCK_XP = 10
    const val EXERCISE_HP = 10
    const val EXERCISE_XP = 20
    const val TOPIC_HP = 25
    const val TOPIC_XP = 50

    fun reward(action: String): Pair<Int, Int> = when (action) {
        "material_completed" -> BLOCK_HP to BLOCK_XP
        "exercise_completed" -> EXERCISE_HP to EXERCISE_XP
        "topic_completed" -> TOPIC_HP to TOPIC_XP
        else -> 0 to 0
    }
}
