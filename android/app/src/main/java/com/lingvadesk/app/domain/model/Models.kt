package com.lingvadesk.app.domain.model

data class Project(
    val id: Long,
    val name: String,
    val nativeLanguage: String,
    val targetLanguage: String,
    val createdAt: Long
)

data class Deck(
    val id: Long,
    val projectId: Long,
    val name: String,
    val pace: Int = 0,
    val newPerDay: Int = 20,
    val maxReviews: Int = 100
)

data class Card(
    val id: Long,
    val deckId: Long,
    val front: String,
    val back: String,
    val example: String? = null,
    val exampleTranslation: String? = null,
    val due: Long = System.currentTimeMillis(),
    val interval: Int = 0,
    val ease: Float = 2.5f,
    val reps: Int = 0,
    val leech: Boolean = false
)

data class Review(
    val id: Long = 0,
    val cardId: Long,
    val timestamp: Long,
    val grade: Int,
    val think: Long,
    val look: Long,
    val fresh: Boolean,
    val mode: String,
    val hinted: Boolean,
    val plays: Int
)
