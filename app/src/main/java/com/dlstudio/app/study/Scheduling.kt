package com.dlstudio.app.study

import com.dlstudio.app.library.LibNode
import java.util.TimeZone
import kotlin.random.Random

/** Date helpers and deterministic content ordering. There is no review scheduler. */
object DayMath {
    const val DAY = 86_400_000L
    fun dayNumber(ms: Long, zone: TimeZone = TimeZone.getDefault()): Long = Math.floorDiv(ms + zone.getOffset(ms), DAY)
    fun weekday(day: Long): Int = Math.floorMod(day + 3, 7L).toInt()
}

/** A session is an explicit list chosen by the user, never a generated review queue. */
object SessionPicker {
    fun pick(cards: List<LibNode>, states: Map<String, CardState>, today: Long, limit: Int, extra: Boolean = false): List<LibNode> = cards.take(limit)
}

object Shuffle {
    fun differentOrder(cards: List<LibNode>, previous: List<String>, random: Random = Random.Default): List<LibNode> {
        if (cards.size < 2) return cards
        val shuffled = cards.shuffled(random)
        return if (shuffled.map { it.id } == previous) shuffled.drop(1) + shuffled.first() else shuffled
    }
}

object ModeWeights {
    fun eligible(card: LibNode, mode: CardMode): Boolean = true
    fun modeFor(card: LibNode, index: Int, options: StudyOptions = StudyOptions()): CardMode = options.modes.firstOrNull() ?: CardMode.FLIP_FWD
    fun optionsFor(card: LibNode, queue: List<LibNode>, reverse: Boolean, index: Int): List<String> = emptyList()
}

object StatsCalc {
    const val DAYS = 14
    fun compute(total: Int, states: Collection<CardState>, perDay: Map<Long, DayAgg>, activeDays: Set<Long>, today: Long): StudyStats {
        val window = (today - DAYS + 1..today).map { perDay[it]?.count ?: 0 }
        return StudyStats(totalCards = total, newCards = total, todayReviews = window.lastOrNull() ?: 0,
            weekReviews = window.takeLast(7).sum(), prevWeekReviews = window.dropLast(7).sum(),
            streakDays = Streak.count(activeDays, today), bestStreak = Streak.longest(activeDays),
            reviewsByDay = window, intervalBuckets = List(4) { 0 }, accuracy = 0)
    }
}

/** Kept as a zero-valued dashboard series; learning has no future schedule. */
object Forecast {
    fun next(states: Collection<CardState>, today: Long, days: Int = 7): List<Int> = List(days) { 0 }
}

object Streak {
    fun count(activeDays: Set<Long>, today: Long): Int { var day = if (today in activeDays) today else today - 1; var result = 0; while (day in activeDays) { result++; day-- }; return result }
    fun longest(activeDays: Set<Long>): Int = activeDays.sorted().fold(0 to 0) { (best, current), day ->
        val next = if (day - 1 in activeDays) current + 1 else 1
        maxOf(best, next) to next
    }.first
}
