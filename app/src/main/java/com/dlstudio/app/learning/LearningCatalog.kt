package com.dlstudio.app.learning

/** Small, side-effect-free catalog helpers used by storage and future screens. */
object LearningCatalog {
    fun coursesFor(subject: Subject, courses: Collection<Course>): List<Course> = courses
        .filter { it.subjectId == subject.id }
        .sortedBy { it.name }

    fun topicsFor(course: Course, topics: Collection<Topic>): List<Topic> = topics
        .filter { it.courseId == course.id }
        .sortedBy { it.order }

    fun materialsFor(topic: Topic, materials: Collection<LearningMaterial>): List<LearningMaterial> = materials
        .filter { it.topicId == topic.id }
        .sortedBy { it.position }

    fun progress(topic: Topic, events: Collection<ProgressEvent>): TopicProgress {
        val own = events.filter { it.topicId == topic.id }.sortedBy { it.occurredAt }
        val completed = own.mapNotNull { it.materialId.takeIf { id -> it.action == "material_completed" && id != null } }.toSet()
        val exercises = own.count { it.action == "exercise_completed" }
        return TopicProgress(topic.id, own.firstOrNull()?.occurredAt, completed, exercises,
            own.size, own.sumOf { it.durationMs ?: 0L },
            own.lastOrNull { it.action == "topic_completed" }?.occurredAt,
            own.lastOrNull()?.occurredAt)
    }

    fun motivation(events: Collection<ProgressEvent>): MotivationTotals {
        var hp = 0
        var xp = 0
        val days = mutableSetOf<Long>()
        val achievements = mutableSetOf<String>()
        events.forEach { event ->
            val reward = MotivationRules.reward(event.action)
            hp += reward.first
            xp += reward.second
            days += event.occurredAt / 86_400_000L
        }
        if (events.any { it.action == "topic_completed" }) achievements += "first_topic"
        return MotivationTotals(hp, xp, days, achievements)
    }
}
