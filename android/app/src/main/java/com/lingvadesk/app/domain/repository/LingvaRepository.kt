package com.lingvadesk.app.domain.repository

import com.lingvadesk.app.domain.model.Card
import com.lingvadesk.app.domain.model.Deck
import com.lingvadesk.app.domain.model.Project
import com.lingvadesk.app.domain.model.Review
import kotlinx.coroutines.flow.Flow

interface LingvaRepository {
    fun getProjects(): Flow<List<Project>>
    fun getDecks(projectId: Long): Flow<List<Deck>>
    fun getCards(deckId: Long): Flow<List<Card>>
    suspend fun insertProject(name: String, nativeLang: String, targetLang: String): Long
    suspend fun insertDeck(projectId: Long, name: String): Long
    suspend fun insertCard(deckId: Long, front: String, back: String, example: String?, exampleTranslation: String?): Long
    suspend fun addReview(review: Review)
}
