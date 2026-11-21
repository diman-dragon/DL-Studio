package com.dlstudio.app.learning

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LearningCatalogTest {
    private val subject = Subject("s", "English")
    private val course = Course("c", subject.id, "Basics", structure = CourseStructure.GRAPH)
    private val topic = Topic("t", course.id, "Greetings", order = 2)

    @Test fun courseAndTopicOwnershipIsExplicit() {
        assertEquals(listOf(course), LearningCatalog.coursesFor(subject, listOf(course)))
        assertEquals(listOf(topic), LearningCatalog.topicsFor(course, listOf(topic)))
    }

    @Test fun materialsAreOrderedAndProgressUsesActions() {
        val first = LearningMaterial("m1", topic.id, MaterialType.THEORY, "Read", 0)
        val exercise = LearningMaterial("m2", topic.id, MaterialType.EXERCISE, "Practice", 1)
        val materials = LearningCatalog.materialsFor(topic, listOf(exercise, first))
        assertEquals(listOf(first, exercise), materials)
        val progress = LearningCatalog.progress(topic, listOf(
            ProgressEvent("e1", topic.id, first.id, "material_completed", 100L, 20L),
            ProgressEvent("e2", topic.id, exercise.id, "exercise_completed", 200L, 30L),
            ProgressEvent("e3", topic.id, null, "topic_completed", 300L)
        ))
        assertEquals(setOf(first.id), progress.completedMaterials)
        assertEquals(1, progress.completedExercises)
        assertEquals(50L, progress.studyTimeMs)
        assertEquals(300L, progress.completedAt)
    }

    @Test fun motivationIsExplicitlyEventBased() {
        val totals = LearningCatalog.motivation(listOf(ProgressEvent("e", topic.id, action = "exercise_completed", occurredAt = 86_400_000L)))
        assertEquals(10, totals.hp)
        assertEquals(20, totals.xp)
        assertTrue(1L in totals.activeDays)
    }
}
