package com.dlstudio.app.progress

import com.dlstudio.app.content.Topic

/** Что считается для награды: опыт, лучшая серия, пройденные темы, просмотренные карточки, верные ответы. */
enum class Metric { XP, STREAK, TOPICS, CARDS, ANSWERS }

/** Награда за дело: выдаётся, когда `metric` дорос до `target`. Порядок важен — по нему подписи в строках интерфейса. */
enum class Badge(val emoji: String, val metric: Metric, val target: Int) {
    FIRST_XP("🌱", Metric.XP, 10),
    TOPIC_1("🏁", Metric.TOPICS, 1),
    STREAK_3("🔥", Metric.STREAK, 3),
    CARDS_25("🃏", Metric.CARDS, 25),
    XP_100("⭐", Metric.XP, 100),
    ANSWERS_20("🎯", Metric.ANSWERS, 20),
    STREAK_7("🌈", Metric.STREAK, 7),
    TOPICS_5("🏆", Metric.TOPICS, 5),
    XP_500("🚀", Metric.XP, 500),
    STREAK_30("👑", Metric.STREAK, 30)
}

/** Итоги, из которых считаются награды. */
data class Totals(val xp: Int, val bestStreak: Int, val topics: Int, val cards: Int, val answers: Int)

/** Награды выводятся только из действий пользователя (прогресса); никаких отметок «сам оценил». */
object Achievements {
    fun totals(p: Progress, topics: List<Topic>, today: Long) = Totals(
        xp = p.xp,
        bestStreak = maxOf(p.bestStreak, Xp.currentStreak(p, today)),
        topics = topics.count { Xp.complete(it, p.topics[it.id] ?: TopicProgress()) },
        cards = p.topics.values.sumOf { it.seen.size },
        answers = p.topics.values.sumOf { it.quizOk.size }
    )

    fun value(badge: Badge, t: Totals): Int = when (badge.metric) {
        Metric.XP -> t.xp
        Metric.STREAK -> t.bestStreak
        Metric.TOPICS -> t.topics
        Metric.CARDS -> t.cards
        Metric.ANSWERS -> t.answers
    }

    fun unlocked(badge: Badge, t: Totals): Boolean = value(badge, t) >= badge.target

    /** Ближайшая ещё не полученная награда — та, до которой меньше всего осталось в долях пути. */
    fun nextUp(t: Totals): Badge? = Badge.values().filter { !unlocked(it, t) }.maxByOrNull { value(it, t).toFloat() / it.target }
}

/** Цель дня и неделя по истории XP. Дни — номера дней (см. Xp.dayNumber). */
object Week {
    const val DAYS = 7

    fun todayXp(p: Progress, today: Long): Int = p.history[today] ?: 0

    /** Понедельник текущей недели (1 января 1970 — четверг, поэтому сдвиг 3). */
    fun monday(today: Long): Long = today - Math.floorMod(today + 3, DAYS.toLong())

    /** XP по дням недели с понедельника по воскресенье; будущие дни — 0. */
    fun xpByDay(p: Progress, today: Long): List<Int> = List(DAYS) { p.history[monday(today) + it] ?: 0 }

    /** Номер дня недели: 0 — понедельник … 6 — воскресенье. */
    fun weekday(today: Long): Int = (today - monday(today)).toInt()

    /** Дневной XP только что дошёл до цели (этим действием): чтобы поздравить один раз. */
    fun crossedGoal(before: Progress, after: Progress, today: Long, goal: Int): Boolean = todayXp(before, today) < goal && todayXp(after, today) >= goal
}

/** Ранг по уровню: индекс в списке названий (после последнего ранга остаётся последний). */
fun rankIndex(level: Int, ranks: Int): Int = (level - 1).coerceIn(0, ranks - 1)
