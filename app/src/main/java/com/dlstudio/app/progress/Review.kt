package com.dlstudio.app.progress

import com.dlstudio.app.content.Topic

/** Что повторить в теме: `wrong` — вопросы, где ошиблись и не исправили, `sound` — звуковые вопросы, пропущенные «без звука». */
data class ReviewItem(val topic: Topic, val wrong: Int, val sound: Int) {
    val total: Int get() = wrong + sound
}

/** «Повторение»: темы с ошибками и пропущенными звуковыми вопросами, больше всего — первыми. Тема, в практику которой ещё не заходили, не считается. */
object Review {
    fun items(topics: List<Topic>, progress: Map<String, TopicProgress>): List<ReviewItem> = topics.mapNotNull { t ->
        val p = progress[t.id] ?: return@mapNotNull null
        val wrong = p.miss.count { it in t.quiz.indices }
        val started = p.quizOk.isNotEmpty() || p.miss.isNotEmpty()
        val sound = if (started && t.tag.isNotBlank()) t.quiz.indices.count { t.quiz[it].needsSound && it !in p.quizOk && it !in p.miss } else 0
        ReviewItem(t, wrong, sound).takeIf { it.total > 0 }
    }.sortedByDescending { it.total }
}
