package com.lingvadesk.app.data.repository

import com.lingvadesk.app.data.local.database.CardEntity
import com.lingvadesk.app.data.local.database.DeckEntity
import com.lingvadesk.app.data.local.database.LingvaDao
import com.lingvadesk.app.data.local.database.ProjectEntity
import com.lingvadesk.app.data.local.database.ReviewEntity
import com.lingvadesk.app.domain.model.Card
import com.lingvadesk.app.domain.model.Deck
import com.lingvadesk.app.domain.model.Project
import com.lingvadesk.app.domain.model.Review
import com.lingvadesk.app.domain.repository.LingvaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LingvaRepositoryImpl @Inject constructor(
    private val dao: LingvaDao
) : LingvaRepository {

    override fun getProjects(): Flow<List<Project>> {
        return dao.getProjectsFlow().map { list ->
            list.map { Project(it.id, it.name, it.native_language, it.target_language, it.created_at) }
        }
    }

    override fun getDecks(projectId: Long): Flow<List<Deck>> {
        return dao.getDecksForProjectFlow(projectId).map { list ->
            list.map { Deck(it.id, it.project_id, it.name, it.pace, it.new_per_day, it.max_reviews) }
        }
    }

    override fun getCards(deckId: Long): Flow<List<Card>> {
        return dao.getCardsForDeckFlow(deckId).map { list ->
            list.map {
                Card(
                    it.id, it.deck_id, it.front, it.back, it.example, it.example_translation,
                    it.due, it.interval, it.ease, it.reps, it.leech
                )
            }
        }
    }

    override suspend fun insertProject(name: String, nativeLang: String, targetLang: String): Long {
        return dao.insertProject(
            ProjectEntity(name = name, native_language = nativeLang, target_language = targetLang, created_at = System.currentTimeMillis())
        )
    }

    override suspend fun insertDeck(projectId: Long, name: String): Long {
        return dao.insertDeck(DeckEntity(project_id = projectId, name = name))
    }

    override suspend fun insertCard(deckId: Long, front: String, back: String, example: String?, exampleTranslation: String?): Long {
        return dao.insertCard(
            CardEntity(
                deck_id = deckId, front = front, back = back, example = example, example_translation = exampleTranslation,
                due = System.currentTimeMillis(), interval = 0, ease = 2.5f, reps = 0, leech = false
            )
        )
    }

    override suspend fun addReview(review: Review) {
        dao.insertReview(
            ReviewEntity(
                card_id = review.cardId, ts = review.timestamp, grade = review.grade,
                think = review.think, look = review.look, fresh = review.fresh,
                mode = review.mode, hinted = review.hinted, plays = review.plays
            )
        )
    }
}
