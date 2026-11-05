package com.lingvadesk.app.domain.study

import com.lingvadesk.app.domain.model.TodayInfo

/** Серии дней и «сегодня» считаются из истории повторений; `days` — номера дней (по возрастанию, без повторов). */
object Streaks {
    fun compute(days: List<Long>, today: Long, todayCount: Int): TodayInfo {
        if (days.isEmpty()) return TodayInfo(todayCount, 0, 0)
        val set = days.toHashSet()

        var best = 0
        var run = 0
        var prev = Long.MIN_VALUE
        for (d in days.sorted()) {
            run = if (prev != Long.MIN_VALUE && d == prev + 1) run + 1 else 1
            if (run > best) best = run
            prev = d
        }

        // серия жива, если сегодня или вчера учились
        var cursor = when {
            today in set -> today
            (today - 1) in set -> today - 1
            else -> return TodayInfo(todayCount, 0, best)
        }
        var current = 0
        while (cursor in set) { current++; cursor-- }
        return TodayInfo(todayCount, current, best)
    }
}
