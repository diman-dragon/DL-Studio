package com.dlstudio.app.progress

import com.dlstudio.app.content.Topic
import java.util.TimeZone
import org.json.JSONArray
import org.json.JSONObject

/** Что сделано в теме: прочитана ли теория, какие карточки пользователь перевернул (увидел ответ), какие вопросы практики решены верно, выдан ли бонус за тему, на каких вопросах ошибались и не исправились (`miss`). */
data class TopicProgress(val theory: Boolean = false, val seen: Set<Int> = emptySet(), val quizOk: Set<Int> = emptySet(), val bonus: Boolean = false, val miss: Set<Int> = emptySet())

/**
 * Очки опыта, серия дней и прогресс по темам (по `id` темы). `bestStreak` — самая длинная серия за всё время,
 * `history` — сколько XP набрано в каждый из последних дней (номер дня → XP): из него рисуются цель дня и неделя, `stats` — статистика действий (см. Stats).
 */
data class Progress(
    val xp: Int = 0, val streak: Int = 0, val lastDay: Long = 0, val topics: Map<String, TopicProgress> = emptyMap(),
    val bestStreak: Int = 0, val history: Map<Long, Int> = emptyMap(), val stats: Stats = Stats()
)

/** Результат действия: новый прогресс, сколько очков получено и закончилась ли этим тема. */
data class Reward(val progress: Progress, val gained: Int, val finished: Boolean)

/** Игровая часть: очки за каждое новое действие, бонус за пройденную тему, уровни по 100 очков. Повторы очков не дают. */
object Xp {
    const val THEORY = 10
    const val CARD = 2
    const val ANSWER = 5
    const val TOPIC = 50
    const val LEVEL = 100
    private const val PASS_PERCENT = 60

    fun level(xp: Int) = xp / LEVEL + 1
    fun intoLevel(xp: Int) = xp % LEVEL

    /** Сколько вопросов практики нужно решить верно, чтобы этап считался пройденным (60%, минимум один). */
    fun passMark(total: Int) = maxOf(1, (total * PASS_PERCENT + 99) / 100)

    fun dayNumber(now: Long = System.currentTimeMillis()): Long = (now + TimeZone.getDefault().getOffset(now)) / MS_PER_DAY

    /** Серия на сегодня: если вчера не занимались, она уже оборвалась. */
    fun currentStreak(p: Progress, today: Long) = if (p.lastDay >= today - 1) p.streak else 0

    /** С какой карточки продолжить: первая, ответ которой ещё не открывали (если все открыты — первая). */
    fun startCard(t: Topic, p: TopicProgress) = t.cards.indices.firstOrNull { it !in p.seen } ?: 0

    /** С какого вопроса продолжить среди показываемых (`order` — номера вопросов): первый, не решённый верно (если все решены — первый). */
    fun startQuestion(order: List<Int>, p: TopicProgress) = order.indexOfFirst { it !in p.quizOk }.coerceAtLeast(0)

    fun theoryDone(t: Topic, p: TopicProgress) = t.theory.isEmpty() || p.theory
    fun cardsDone(t: Topic, p: TopicProgress) = t.cards.isEmpty() || p.seen.size >= t.cards.size
    fun quizDone(t: Topic, p: TopicProgress) = t.quiz.isEmpty() || p.quizOk.size >= passMark(t.quiz.size)
    fun complete(t: Topic, p: TopicProgress) = theoryDone(t, p) && cardsDone(t, p) && quizDone(t, p)

    /** Доля пройденных этапов темы, 0..3. */
    fun stagesDone(t: Topic, p: TopicProgress) = listOf(theoryDone(t, p), cardsDone(t, p), quizDone(t, p)).count { it }

    fun theory(p: Progress, t: Topic, today: Long) = award(p, t, today, THEORY) { it.copy(theory = true) }
    fun card(p: Progress, t: Topic, index: Int, today: Long) = award(p, t, today, CARD) { it.copy(seen = it.seen + index) }
    fun answer(p: Progress, t: Topic, index: Int, correct: Boolean, today: Long): Reward {
        if (correct) return award(p, t, today, ANSWER) { it.copy(quizOk = it.quizOk + index, miss = it.miss - index) }
        val before = p.topics[t.id] ?: TopicProgress()
        return Reward(p.copy(topics = p.topics + (t.id to before.copy(miss = before.miss + index))), 0, false)
    }

    private fun award(p: Progress, t: Topic, today: Long, base: Int, change: (TopicProgress) -> TopicProgress): Reward {
        val before = p.topics[t.id] ?: TopicProgress()
        var after = change(before)
        if (after == before) return Reward(p, 0, false)
        var gained = base
        val finished = complete(t, after) && !after.bonus
        if (finished) { gained += TOPIC; after = after.copy(bonus = true) }
        return Reward(earn(p.copy(topics = p.topics + (t.id to after)), gained, today), gained, finished)
    }

    private fun earn(p: Progress, amount: Int, today: Long): Progress {
        val streak = when (p.lastDay) {
            today -> maxOf(1, p.streak)
            today - 1 -> p.streak + 1
            else -> 1
        }
        val history = (p.history + (today to ((p.history[today] ?: 0) + amount))).filterKeys { it > today - HISTORY_DAYS }
        return p.copy(xp = p.xp + amount, streak = streak, lastDay = today, bestStreak = maxOf(p.bestStreak, streak), history = history)
    }

    private const val MS_PER_DAY = 86_400_000L
    private const val HISTORY_DAYS = 60
}

/** Прогресс в одну JSON-строку и обратно; испорченная строка даёт пустой прогресс. */
object ProgressJson {
    private fun JSONArray.ints(): Set<Int> = (0 until length()).map { getInt(it) }.toSet()

    fun encode(p: Progress): String = JSONObject()
        .put("xp", p.xp).put("streak", p.streak).put("day", p.lastDay).put("bs", p.bestStreak)
        .put("st", StatsJson.encode(p.stats))
        .put("h", JSONObject().also { all -> p.history.forEach { (day, xp) -> all.put(day.toString(), xp) } })
        .put("topics", JSONObject().also { all ->
            p.topics.forEach { (id, t) ->
                all.put(id, JSONObject().put("t", t.theory).put("k", JSONArray(t.seen.sorted())).put("q", JSONArray(t.quizOk.sorted())).put("b", t.bonus).put("m", JSONArray(t.miss.sorted())))
            }
        }).toString()

    fun decode(text: String?): Progress = runCatching {
        val o = JSONObject(text!!)
        val all = o.getJSONObject("topics")
        val history = o.getJSONObject("h").let { h -> h.keys().asSequence().associate { it.toLong() to h.getInt(it) } }
        Progress(o.getInt("xp"), o.getInt("streak"), o.getLong("day"), all.keys().asSequence().associateWith { id ->
            val t = all.getJSONObject(id)
            TopicProgress(t.getBoolean("t"), t.getJSONArray("k").ints(), t.getJSONArray("q").ints(), t.getBoolean("b"), t.optJSONArray("m")?.ints() ?: emptySet())
        }, o.getInt("bs"), history, StatsJson.decode(o.optJSONObject("st")))
    }.getOrDefault(Progress())
}
