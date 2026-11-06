package com.dlstudio.app.study

import androidx.lifecycle.ViewModel
import com.dlstudio.app.library.LibNode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Состояние учёбы живёт здесь, поэтому не теряется при смене вкладки и повороте. */
class StudyViewModel : ViewModel() {
    private val mutable = MutableStateFlow(StudyUi())
    val ui: StateFlow<StudyUi> = mutable.asStateFlow()

    fun openProject(id: String?) = mutable.update { StudyUi(scoped = true, projectId = id) }
    fun openDeck(id: String?) = mutable.update { it.copy(deckId = id, scoped = true) }
    /** Шаг назад: из сессии — к выбору, из колоды — к колодам, из проекта — к проектам. */
    fun back() = mutable.update { if (it.session != null) it.copy(session = null) else if (it.deckId != null) it.copy(deckId = null) else StudyUi() }
    fun close() = mutable.update { it.copy(session = null) }

    fun begin(cards: List<LibNode>) = mutable.update { it.copy(session = if (cards.isEmpty()) null else StudySession.start(cards, System.currentTimeMillis())) }
    fun flip() = mutable.update { u -> u.copy(session = u.session?.flip(System.currentTimeMillis())) }

    /** Ответ на открытую карточку; возвращает запись для базы (или null, если карточка не перевёрнута). */
    fun answer(grade: Grade): Answer? {
        val result = mutable.value.session?.answer(grade, System.currentTimeMillis()) ?: return null
        mutable.update { it.copy(session = result.first) }
        return result.second
    }
}
