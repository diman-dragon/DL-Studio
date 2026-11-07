package com.dlstudio.app.study

import androidx.lifecycle.ViewModel
import com.dlstudio.app.library.LibNode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

/** Состояние учёбы живёт здесь, поэтому не теряется при смене вкладки и повороте. */
class StudyViewModel : ViewModel() {
    private val mutable = MutableStateFlow(StudyUi())
    val ui: StateFlow<StudyUi> = mutable.asStateFlow()
    private var restoreTried = false

    fun openProject(id: String?) = mutable.update { StudyUi(scoped = true, projectId = id) }
    fun openDeck(id: String?) = mutable.update { it.copy(deckId = id, scoped = true) }

    /** Шаг назад: из сессии — к выбору, из колоды — к колодам, из проекта — к проектам. */
    fun back() = mutable.update { if (it.session != null) it.copy(session = null) else if (it.deckId != null) it.copy(deckId = null) else StudyUi() }
    fun close() = mutable.update { it.copy(session = null) }

    /** Новая сессия: возвращает её, чтобы записать состав и порядок карточек в базу. */
    fun begin(cards: List<LibNode>): StudySession? {
        if (cards.isEmpty()) return null
        val session = StudySession.start(UUID.randomUUID().toString(), cards, System.currentTimeMillis())
        mutable.update { it.copy(session = session) }
        return session
    }

    /** После запуска приложения продолжает незавершённую сессию сегодняшнего дня с того же места и в том же порядке. */
    fun restore(items: List<LibNode>, saved: SavedSession?) {
        if (restoreTried) return
        restoreTried = true
        val today = DayMath.dayNumber(System.currentTimeMillis())
        if (saved == null || mutable.value.session != null || DayMath.dayNumber(saved.startedAt) != today || saved.grades.size >= saved.cardIds.size) return
        val byId = StudyScope.cards(items, null, null).associateBy { it.id }
        val cards = saved.cardIds.mapNotNull { byId[it] }
        if (cards.isNotEmpty()) mutable.update { it.copy(session = StudySession.restore(saved, cards, System.currentTimeMillis())) }
    }

    fun goTo(page: Int) = mutable.update { u -> u.copy(session = u.session?.goTo(page, System.currentTimeMillis())) }
    fun hint() = mutable.update { u -> u.copy(session = u.session?.useHint()) }

    /** Тап по карточке; возвращает запись для базы при первом показе перевода. */
    fun flip(): Answer? {
        val result = mutable.value.session?.flip(System.currentTimeMillis()) ?: return null
        mutable.update { it.copy(session = result.first) }
        return result.second
    }
}
