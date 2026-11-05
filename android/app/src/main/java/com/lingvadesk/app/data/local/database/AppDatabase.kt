package com.lingvadesk.app.data.local.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.Database
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "languages")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val native_language: String,
    val target_language: String,
    val created_at: Long
)

@Entity(tableName = "decks")
data class DeckEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val project_id: Long,
    val name: String,
    val pace: Int = 0,
    val new_per_day: Int = 20,
    val max_reviews: Int = 100
)

@Entity(tableName = "cards")
data class CardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val deck_id: Long,
    val front: String,
    val back: String,
    val example: String?,
    val example_translation: String?,
    val due: Long,
    val interval: Int,
    val ease: Float,
    val reps: Int,
    val leech: Boolean
)

@Entity(tableName = "reviews")
data class ReviewEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val card_id: Long,
    val ts: Long,
    val grade: Int,
    val think: Long,
    val look: Long,
    val fresh: Boolean,
    val mode: String,
    val hinted: Boolean,
    val plays: Int
)

@Dao
interface LingvaDao {
    @Query("SELECT * FROM languages")
    fun getProjectsFlow(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM decks WHERE project_id = :projectId")
    fun getDecksForProjectFlow(projectId: Long): Flow<List<DeckEntity>>

    @Query("SELECT * FROM cards WHERE deck_id = :deckId")
    fun getCardsForDeckFlow(deckId: Long): Flow<List<CardEntity>>

    @Insert
    suspend fun insertProject(project: ProjectEntity): Long

    @Insert
    suspend fun insertDeck(deck: DeckEntity): Long

    @Insert
    suspend fun insertCard(card: CardEntity): Long

    @Insert
    suspend fun insertReview(review: ReviewEntity)
}

@Database(entities = [ProjectEntity::class, DeckEntity::class, CardEntity::class, ReviewEntity::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun lingvaDao(): LingvaDao
}
