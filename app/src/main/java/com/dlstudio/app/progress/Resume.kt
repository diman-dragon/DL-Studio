package com.dlstudio.app.progress

import com.dlstudio.app.content.Topic

/** Урок — группа тем одного курса («Урок 1»). Ключ уникален среди курсов: по нему карта помнит, какой урок раскрыт. */
data class Lesson(val course: String, val group: String, val topics: List<Topic>) {
    val key: String get() = "$course/$group"
}

/**
 * Где человек остановился. «Последняя тема» — та, что открывали в последний раз (хранится по `id`); её урок (группа) на карте раскрыт.
 * Пока последняя тема не пройдена, «Продолжить» ведёт в неё; когда пройдена — в ближайшую открытую следом, дальше в любую открытую.
 */
object Resume {
    /** Последняя открытая тема, если она всё ещё установлена. */
    fun last(topics: List<Topic>, lastId: String): Topic? = topics.firstOrNull { it.id == lastId }

    fun next(topics: List<Topic>, progress: Map<String, TopicProgress>, lastId: String): Topic? {
        val last = last(topics, lastId)
        if (last != null && !Xp.complete(last, progress[last.id] ?: TopicProgress())) return last
        val courses = topics.groupBy { it.course }.values
        val own = courses.firstOrNull { c -> c.any { it.id == last?.id } }
        if (own != null) {
            val open = TopicGraph.open(own, progress)
            val after = own.indexOf(last)
            (open.firstOrNull { own.indexOf(it) > after } ?: open.firstOrNull())?.let { return it }
        }
        return courses.firstNotNullOfOrNull { TopicGraph.open(it, progress).firstOrNull() }
    }

    /** Тема, на которой карта встаёт при открытии: последняя открытая, а если такой нет — та, что предложит «Продолжить». */
    fun focus(topics: List<Topic>, progress: Map<String, TopicProgress>, lastId: String): Topic? =
        last(topics, lastId) ?: next(topics, progress, lastId)

    /** Уроки в порядке тем: тема за темой, группа возникает там, где встретилась впервые. */
    fun lessons(topics: List<Topic>): List<Lesson> = topics.groupBy { it.course to it.group }.map { (key, list) -> Lesson(key.first, key.second, list) }

    /** Урок, где остановились: тот, что у темы `focus`; без неё — первый. */
    fun active(lessons: List<Lesson>, focus: Topic?): Lesson? = lessons.firstOrNull { l -> focus != null && l.topics.any { it.id == focus.id } } ?: lessons.firstOrNull()
}
