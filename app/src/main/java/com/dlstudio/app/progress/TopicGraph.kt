package com.dlstudio.app.progress

import com.dlstudio.app.content.Topic

/** Курс-граф: у тем есть предпосылки (`requires`). Курс без предпосылок рисуется тропой. */
object TopicGraph {
    /** Есть ли у тем курса хотя бы одна предпосылка из самого курса. */
    fun isGraph(topics: List<Topic>): Boolean {
        val ids = topics.map { it.id }.toSet()
        return topics.any { t -> t.requires.any { it in ids && it != t.id } }
    }

    /** Слой темы: 0 без предпосылок, иначе на один глубже самой глубокой предпосылки. Неизвестные `id` и циклы игнорируются. */
    fun layers(topics: List<Topic>): Map<String, Int> {
        val byId = topics.associateBy { it.id }
        val memo = HashMap<String, Int>()
        fun depth(id: String, seen: Set<String>): Int {
            memo[id]?.let { return it }
            val parents = byId.getValue(id).requires.filter { it in byId && it != id && it !in seen }
            val layer = if (parents.isEmpty()) 0 else 1 + parents.maxOf { depth(it, seen + id) }
            memo[id] = layer
            return layer
        }
        return topics.associate { it.id to depth(it.id, emptySet()) }
    }

    /** Темы, к которым можно приступать: не пройдены, а все предпосылки (известные в курсе) пройдены. На тропе — все непройденные. */
    fun open(topics: List<Topic>, progress: Map<String, TopicProgress>): List<Topic> {
        val byId = topics.associateBy { it.id }
        fun done(t: Topic) = Xp.complete(t, progress[t.id] ?: TopicProgress())
        return topics.filter { t -> !done(t) && t.requires.all { r -> byId[r]?.let { done(it) } ?: true } }
    }
}
