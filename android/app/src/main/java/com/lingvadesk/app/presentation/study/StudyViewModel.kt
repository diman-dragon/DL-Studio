package com.lingvadesk.app.presentation.study

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lingvadesk.app.domain.model.Review
import com.lingvadesk.app.domain.repository.LingvaRepository
import com.lingvadesk.app.domain.usecase.SrsUseCase
import com.lingvadesk.app.domain.usecase.StudySessionItem
import com.lingvadesk.app.domain.usecase.StudySessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface StudyUiState {
    object Loading : StudyUiState
    data class ActiveSession(
        val items: List<StudySessionItem>,
        val currentIndex: Int,
        val correctCount: Int
    ) : StudyUiState
    object Finished : StudyUiState
}

@HiltViewModel
class StudyViewModel @Inject constructor(
    private val repository: LingvaRepository,
    private val sessionUseCase: StudySessionUseCase,
    private val srsUseCase: SrsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<StudyUiState>(StudyUiState.Loading)
    val uiState: StateFlow<StudyUiState> = _uiState.asStateFlow()

    private var startTime: Long = System.currentTimeMillis()
    private var lookCount: Int = 0
    private var playCount: Int = 0

    fun startSessionForDeck(deckId: Long) {
        viewModelScope.launch {
            val cards = repository.getCards(deckId).first()
            val sessionItems = sessionUseCase.buildSession(cards)
            startTime = System.currentTimeMillis()
            _uiState.value = StudyUiState.ActiveSession(
                items = sessionItems,
                currentIndex = 0,
                correctCount = 0
            )
        }
    }

    fun submitAnswer(grade: Int, hinted: Boolean) {
        val currentState = _uiState.value as? StudyUiState.ActiveSession ?: return
        val currentItem = currentState.items.getOrNull(currentState.currentIndex) ?: return
        val thinkTime = System.currentTimeMillis() - startTime

        viewModelScope.launch {
            repository.addReview(
                Review(
                    cardId = currentItem.card.id,
                    timestamp = System.currentTimeMillis(),
                    grade = grade,
                    think = thinkTime,
                    look = lookCount.toLong(),
                    fresh = currentItem.card.interval == 0,
                    mode = currentItem.mode,
                    hinted = hinted,
                    plays = playCount
                )
            )

            val srs = srsUseCase.calculateNextSrs(
                currentItem.card.interval,
                currentItem.card.ease,
                currentItem.card.reps,
                grade
            )

            val nextIndex = currentState.currentIndex + 1
            if (nextIndex >= currentState.items.size) {
                _uiState.value = StudyUiState.Finished
            } else {
                startTime = System.currentTimeMillis()
                lookCount = 0
                playCount = 0
                _uiState.value = currentState.copy(
                    currentIndex = nextIndex,
                    correctCount = if (grade >= 3) currentState.correctCount + 1 else currentState.correctCount
                )
            }
        }
    }
}
