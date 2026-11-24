package com.dlstudio.app.progress

import com.dlstudio.app.speech.Grade
import org.json.JSONArray
import org.json.JSONObject

/** Верно и неверно. */
data class Counts(val ok: Int = 0, val wrong: Int = 0) {
    val total: Int get() = ok + wrong

    /** Доля верных ответов в процентах; без ответов — 0. */
    val percent: Int get() = if (total == 0) 0 else ok * PERCENT / total

    fun add(right: Boolean) = if (right) copy(ok = ok + 1) else copy(wrong = wrong + 1)

    private companion object { const val PERCENT = 100 }
}

/** Что сделано за день: ответы верно/неверно, просмотрено карточек, попыток произношения. */
data class DayStats(val answers: Counts = Counts(), val cards: Int = 0, val tries: Int = 0)

/** Произношение: попыток, сумма оценок, лучшая оценка и сколько попыток вышло «отлично», «хорошо», «ещё раз». */
data class PronStats(val tries: Int = 0, val sum: Int = 0, val best: Int = 0, val great: Int = 0, val good: Int = 0, val retry: Int = 0) {
    val average: Int get() = if (tries == 0) 0 else sum / tries
}

/**
 * Статистика: ответы по видам заданий и по темам, по дням (последние 60), просмотры карточек и страниц теории, прослушивания и произношение.
 * Всё считается из действий пользователя, повторы тоже (в отличие от XP, который за повтор не даётся).
 */
data class Stats(
    val byKind: Map<String, Counts> = emptyMap(), val byTopic: Map<String, Counts> = emptyMap(), val days: Map<Long, DayStats> = emptyMap(),
    val cards: Int = 0, val pages: Int = 0, val listens: Int = 0, val pron: PronStats = PronStats()
) {
    val answers: Counts get() = byKind.values.fold(Counts()) { all, c -> Counts(all.ok + c.ok, all.wrong + c.wrong) }

    private fun day(today: Long, change: (DayStats) -> DayStats): Map<Long, DayStats> =
        (days + (today to change(days[today] ?: DayStats()))).filterKeys { it > today - KEEP_DAYS }

    fun answered(kind: String, topicId: String, right: Boolean, today: Long) = copy(
        byKind = byKind + (kind to (byKind[kind] ?: Counts()).add(right)),
        byTopic = byTopic + (topicId to (byTopic[topicId] ?: Counts()).add(right)),
        days = day(today) { it.copy(answers = it.answers.add(right)) }
    )

    fun cardSeen(today: Long) = copy(cards = cards + 1, days = day(today) { it.copy(cards = it.cards + 1) })
    fun pageSeen() = copy(pages = pages + 1)
    fun listened() = copy(listens = listens + 1)

    /** Попытка произношения с оценкой 0..100 и её разряд (см. Pronunciation.grade). */
    fun pronounced(score: Int, grade: Grade, today: Long) = copy(
        pron = PronStats(
            pron.tries + 1, pron.sum + score, maxOf(pron.best, score),
            pron.great + if (grade == Grade.GREAT) 1 else 0, pron.good + if (grade == Grade.GOOD) 1 else 0, pron.retry + if (grade == Grade.RETRY) 1 else 0
        ),
        days = day(today) { it.copy(tries = it.tries + 1) }
    )

    /** Ответы по дням за последние `n` дней, самый старый первым; дни без занятий — пустые. */
    fun lastDays(today: Long, n: Int): List<DayStats> = List(n) { days[today - (n - 1 - it)] ?: DayStats() }

    private companion object { const val KEEP_DAYS = 60 }
}

/** Статистика в JSON и обратно; испорченное или отсутствующее даёт пустую статистику. */
object StatsJson {
    private fun counts(c: Counts) = JSONArray().put(c.ok).put(c.wrong)
    private fun counts(a: JSONArray) = Counts(a.getInt(0), a.getInt(1))

    private fun JSONObject.mapOfCounts(): Map<String, Counts> = keys().asSequence().associateWith { counts(getJSONArray(it)) }

    fun encode(s: Stats): JSONObject = JSONObject()
        .put("k", JSONObject().also { o -> s.byKind.forEach { (k, c) -> o.put(k, counts(c)) } })
        .put("t", JSONObject().also { o -> s.byTopic.forEach { (k, c) -> o.put(k, counts(c)) } })
        .put("d", JSONObject().also { o -> s.days.forEach { (day, d) -> o.put(day.toString(), counts(d.answers).put(d.cards).put(d.tries)) } })
        .put("c", s.cards).put("p", s.pages).put("l", s.listens)
        .put("pr", JSONArray().put(s.pron.tries).put(s.pron.sum).put(s.pron.best).put(s.pron.great).put(s.pron.good).put(s.pron.retry))

    fun decode(o: JSONObject?): Stats = runCatching {
        val d = o!!.getJSONObject("d")
        val pr = o.getJSONArray("pr")
        Stats(
            o.getJSONObject("k").mapOfCounts(), o.getJSONObject("t").mapOfCounts(),
            d.keys().asSequence().associate { key -> d.getJSONArray(key).let { key.toLong() to DayStats(counts(it), it.getInt(2), it.getInt(3)) } },
            o.getInt("c"), o.getInt("p"), o.getInt("l"), PronStats(pr.getInt(0), pr.getInt(1), pr.getInt(2), pr.getInt(3), pr.getInt(4), pr.getInt(5))
        )
    }.getOrDefault(Stats())
}
