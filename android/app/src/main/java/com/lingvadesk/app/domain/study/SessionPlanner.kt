package com.lingvadesk.app.domain.study

import com.lingvadesk.app.domain.model.StudyCard
import kotlin.random.Random

/**
 * План сессии (порт `study/buildOrder`, `takePlan`, `orderFresh`): просроченные (давние первыми) + новые,
 * не больше `perDay` новых на колоду за день и не больше `max` карточек на колоду и в сессии.
 * Сессия идёт строго по плану: «Снова» в сессии не возвращается.
 */
object SessionPlanner {
    fun plan(cards: List<StudyCard>, now: Long, limits: Limits, random: Random = Random.Default): List<StudyCard> {
        val byDeck = cards.groupBy { it.card.deckId }
        val dueAll = ArrayList<StudyCard>()
        val freshAll = ArrayList<StudyCard>()

        for ((deckId, list) in byDeck) {
            val own = limits.decks[deckId] ?: DeckLimits()
            val perDay = own.perDay ?: limits.perDay
            val deckMax = own.max ?: limits.max

            val due = list.filter { it.card.isDue(now) }.sortedBy { it.card.due }.take(deckMax)
            val room = (perDay - (limits.used[deckId] ?: 0)).coerceAtLeast(0)
            val fresh = list.filter { it.card.isNew }
                .let { if (own.inOrder) it.sortedWith(compareBy({ s -> s.card.ord }, { s -> s.card.id })) else it.shuffled(random) }
                .take(minOf(room, (deckMax - due.size).coerceAtLeast(0)))
            dueAll += due
            freshAll += fresh
        }
        return (dueAll.sortedBy { it.card.due } + freshAll).take(limits.max)
    }
}
