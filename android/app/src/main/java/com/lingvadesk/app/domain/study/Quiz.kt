package com.lingvadesk.app.domain.study

import com.lingvadesk.app.domain.model.StudyCard
import com.lingvadesk.app.domain.srs.SpeechScore
import kotlin.random.Random

enum class TaskKind { FLIP, CHOICE, TYPE, LISTEN, CLOZE, SPEAK }

/**
 * Задание на карточку в сессии (порт `study/makeQuiz`, `quizPick`): вид, направление и порядок вариантов
 * фиксируются при первом показе — свайпнули дальше и вернулись, задание то же.
 *
 */
data class QuizTask(
    val kind: TaskKind,
    val prompt: String,
    val answer: String,
    val choices: List<String> = emptyList(),
    /** Что произносить (озвучка/распознавание) и на каком языке. */
    val speakText: String? = null
)

object Quiz {
    /** Вес вида задания: перевод, на слух, пропуск и произношение — поровну, ввод с клавиатуры — в 4 раза реже. */
    private const val WEIGHT = 4
    private const val TYPE_WEIGHT = 1
    const val MIN_OTHERS_FOR_CHOICE = 3

    fun answerOf(card: StudyCard, reverse: Boolean) = if (reverse) card.card.front else card.card.back
    fun promptOf(card: StudyCard, reverse: Boolean) = if (reverse) card.card.back else card.card.front

    /** Чужие ответы (разные, не совпадающие с правильным) для вариантов выбора. */
    fun others(pool: List<StudyCard>, card: StudyCard, reverse: Boolean): List<String> {
        val right = SpeechScore.normalize(answerOf(card, reverse))
        return pool.asSequence()
            .filter { it.card.id != card.card.id }
            .map { answerOf(it, reverse) }
            .filter { it.isNotBlank() && SpeechScore.normalize(it) != right }
            .distinctBy { SpeechScore.normalize(it) }
            .toList()
    }

    /** Выбирает вид задания: из доступных — тот, что показывали реже всего с учётом веса. */
    fun pick(
        card: StudyCard,
        pool: List<StudyCard>,
        reverse: Boolean,
        speechAvailable: Boolean,
        shown: Map<TaskKind, Int>,
        random: Random = Random.Default
    ): TaskKind {
        val canChoose = others(pool, card, reverse).size >= MIN_OTHERS_FOR_CHOICE
        val hasCloze = !card.card.cloze.isNullOrBlank() && !card.card.clozeAnswer.isNullOrBlank()
        val weights = LinkedHashMap<TaskKind, Int>()
        weights[if (canChoose) TaskKind.CHOICE else TaskKind.FLIP] = WEIGHT
        weights[TaskKind.TYPE] = TYPE_WEIGHT
        if (!reverse) weights[TaskKind.LISTEN] = WEIGHT
        if (hasCloze) weights[TaskKind.CLOZE] = WEIGHT
        if (speechAvailable && !reverse) weights[TaskKind.SPEAK] = WEIGHT
        // «Дефицит»: меньше всего показов на единицу веса; при равенстве — случайный
        val scored = weights.entries.map { (k, w) -> Triple(k, (shown[k] ?: 0).toDouble() / w, random.nextDouble()) }
        return scored.minWith(compareBy({ it.second }, { it.third })).first
    }

    fun make(
        card: StudyCard,
        pool: List<StudyCard>,
        reverse: Boolean,
        kind: TaskKind,
        random: Random = Random.Default
    ): QuizTask? {
        val c = card.card
        return when (kind) {
            TaskKind.FLIP -> QuizTask(kind, promptOf(card, reverse), answerOf(card, reverse), speakText = c.front)
            TaskKind.CHOICE -> {
                val others = others(pool, card, reverse)
                if (others.size < MIN_OTHERS_FOR_CHOICE) null
                else {
                    val answer = answerOf(card, reverse)
                    val choices = (others.shuffled(random).take(MIN_OTHERS_FOR_CHOICE) + answer).shuffled(random)
                    QuizTask(kind, promptOf(card, reverse), answer, choices, c.front)
                }
            }
            TaskKind.TYPE -> QuizTask(kind, promptOf(card, reverse), answerOf(card, reverse), speakText = c.front)
            TaskKind.LISTEN -> QuizTask(kind, "", c.front, speakText = c.front)
            TaskKind.CLOZE -> {
                val answer = c.clozeAnswer
                if (c.cloze.isNullOrBlank() || answer.isNullOrBlank()) null
                else QuizTask(kind, c.cloze, answer, speakText = c.cloze.replace("_", answer))
            }
            TaskKind.SPEAK -> QuizTask(kind, c.back, c.front, speakText = c.front)
        }
    }

    /** Ответ верный: без регистра и пунктуации; допускаются варианты через «;», «/», «,». */
    fun isCorrect(task: QuizTask, typed: String): Boolean {
        val got = SpeechScore.normalize(typed)
        if (got.isEmpty()) return false
        if (got == SpeechScore.normalize(task.answer)) return true
        return task.answer.split(';', '/', ',').map { SpeechScore.normalize(it) }.filter { it.isNotEmpty() }.any { it == got }
    }
}
