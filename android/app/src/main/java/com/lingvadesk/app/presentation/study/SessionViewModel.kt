package com.lingvadesk.app.presentation.study

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lingvadesk.app.domain.model.Card
import com.lingvadesk.app.domain.model.Grade
import com.lingvadesk.app.domain.model.Review
import com.lingvadesk.app.domain.model.StudyCard
import com.lingvadesk.app.domain.repository.LingvaRepository
import com.lingvadesk.app.domain.srs.GradeInference
import com.lingvadesk.app.domain.srs.Signals
import com.lingvadesk.app.domain.srs.SpeechScore
import com.lingvadesk.app.domain.srs.Srs
import com.lingvadesk.app.domain.srs.SrsState
import com.lingvadesk.app.domain.study.Limits
import com.lingvadesk.app.domain.study.Quiz
import com.lingvadesk.app.domain.study.QuizTask
import com.lingvadesk.app.domain.study.SessionPlanner
import com.lingvadesk.app.domain.study.TaskKind
import com.lingvadesk.app.platform.TtsEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

data class SessionUi(
    val loading: Boolean = true,
    val current: StudyCard? = null,
    val task: QuizTask? = null,
    val left: Int = 0,
    val total: Int = 0,
    val flipped: Boolean = false,
    val picked: String? = null,
    val correct: Boolean? = null,
    val textShown: Boolean = false,
    val heard: String? = null,
    val score: Double? = null,
    val finished: Boolean = false,
    val mistakes: Int = 0,
    val retry: Boolean = false,
    val canUndo: Boolean = false,
    val error: String? = null
)

/** Сессия учёбы. Порядок и план строятся из базы заново; оценка — по поведению или по результату задания; «Снова» в сессии не возвращается. */
@HiltViewModel
class SessionViewModel @Inject constructor(
    private val repo: LingvaRepository,
    private val tts: TtsEngine
) : ViewModel() {

    private val _ui = MutableStateFlow(SessionUi())
    val ui: StateFlow<SessionUi> = _ui.asStateFlow()

    private var order = ArrayList<StudyCard>()
    private var pool: List<StudyCard> = emptyList()
    private var limits = Limits()
    private var pos = 0
    private var total = 0
    private var retry = false
    private var speechAvailable = false
    private val tasks = HashMap<Long, QuizTask>()
    private val reverse = HashMap<Long, Boolean>()
    private val shown = HashMap<TaskKind, Int>()
    private val graded = HashSet<Long>()
    private val mistakes = LinkedHashSet<Long>()
    private val visited = HashSet<Long>()
    private var undo: Undo? = null
    private var started = false

    // отслеживание поведения на карточке
    private var showAt = 0L
    private var flipAt = 0L
    private var flips = 0
    private var revisit = false
    private var away = false
    private var pausedAt = 0L
    private var attempts = 0

    private class Undo(val index: Int, val card: StudyCard, val prev: Card, val reviewId: Long)

    fun start(scope: String, speechAvailable: Boolean) {
        if (started) return
        started = true
        this.speechAvailable = speechAvailable
        viewModelScope.launch {
            try {
                val now = System.currentTimeMillis()
                pool = repo.studyCards(scope)
                limits = repo.loadLimits(scope, now)
                order = ArrayList(SessionPlanner.plan(pool, now, limits))
                total = order.size
                pos = 0
                begin()
            } catch (e: Exception) {
                _ui.value = SessionUi(loading = false, error = e.message ?: "Не удалось начать сессию")
            }
        }
    }

    fun retryMistakes() {
        val ids = mistakes.toList()
        val byId = pool.associateBy { it.card.id }
        order = ArrayList(ids.mapNotNull { byId[it] })
        mistakes.clear(); graded.clear(); tasks.clear(); reverse.clear(); visited.clear(); undo = null
        retry = true
        total = order.size
        pos = 0
        begin()
    }

    // ---------- показ карточки ----------
    private fun begin() {
        if (order.isEmpty()) { finish(); return }
        pos = pos.coerceIn(0, order.lastIndex)
        val sc = order[pos]
        val id = sc.card.id
        revisit = id in visited
        visited += id
        val rev = reverse.getOrPut(id) { Random.nextBoolean() }
        val task = tasks.getOrPut(id) {
            var kind = Quiz.pick(sc, pool, rev, speechAvailable, shown)
            var t = Quiz.make(sc, pool, rev, kind)
            if (t == null) { kind = TaskKind.FLIP; t = Quiz.make(sc, pool, rev, kind)!! }
            shown[kind] = (shown[kind] ?: 0) + 1
            t
        }
        showAt = System.currentTimeMillis(); flipAt = 0; flips = 0; away = false; attempts = 0
        _ui.value = SessionUi(
            loading = false, current = sc, task = task, left = order.count { it.card.id !in graded },
            total = total, mistakes = mistakes.size, retry = retry, canUndo = undo != null
        )
        if (task.kind == TaskKind.LISTEN) say(task.speakText, sc.tts)
    }

    private fun finish() {
        _ui.value = SessionUi(loading = false, finished = true, total = total, mistakes = mistakes.size, retry = retry)
    }

    fun say(text: String?, lang: String, rate: Float = 1f) { if (text != null) { tts.speak(text, lang, rate); plays++ } }
    fun sayText(lang: String, rate: Float) { _ui.value.task?.speakText?.let { say(it, lang, rate) } }
    fun saySlow(gapMs: Long, lang: String) { _ui.value.task?.speakText?.let { tts.speakSlow(it, lang, gapMs) } }
    private var plays = 0

    // ---------- поведение ----------
    fun onPause() { pausedAt = System.currentTimeMillis() }
    fun onResume() {
        if (pausedAt != 0L && System.currentTimeMillis() - pausedAt > 30_000L) away = true
        if (pausedAt != 0L) showAt += System.currentTimeMillis() - pausedAt   // время в фоне не считаем
        pausedAt = 0L
    }

    fun flip() {
        val ui = _ui.value
        if (ui.task?.kind != TaskKind.FLIP) return
        val now = System.currentTimeMillis()
        if (flips == 0) flipAt = now
        flips++
        _ui.update { it.copy(flipped = !it.flipped) }
    }

    fun showText() { _ui.update { it.copy(textShown = true) } }

    private fun signals(now: Long): Signals {
        val think = if (flips > 0) flipAt - showAt else null
        val look = if (flips > 0) now - flipAt else 0L
        return Signals(think = think, look = look, dwell = now - showAt, flips = flips, revisit = revisit, away = away)
    }

    // ---------- ответы ----------
    fun answer(text: String) {
        val ui = _ui.value
        val sc = ui.current ?: return
        val task = ui.task ?: return
        if (sc.card.id in graded || task.kind == TaskKind.FLIP || task.kind == TaskKind.SPEAK) return
        val ok = Quiz.isCorrect(task, text)
        val now = System.currentTimeMillis()
        val grade = when {
            !ok -> Grade.AGAIN
            ui.textShown -> Grade.HARD
            sc.card.reps >= GradeInference.MATURE_REPS && now - showAt < 4_000L -> Grade.EASY
            else -> Grade.GOOD
        }
        _ui.update { it.copy(picked = text, correct = ok) }
        commit(sc, grade, now, hinted = ui.textShown)
    }

    /** Результат распознавания речи: варианты и уверенность. */
    fun onSpeech(results: List<String>, confidences: FloatArray?) {
        val ui = _ui.value
        val sc = ui.current ?: return
        val task = ui.task ?: return
        if (task.kind != TaskKind.SPEAK || sc.card.id in graded) return
        attempts++
        val conf = confidences?.firstOrNull()?.toDouble() ?: -1.0
        val score = SpeechScore.score(results.firstOrNull().orEmpty(), results.drop(1), conf, task.answer)
        val now = System.currentTimeMillis()
        viewModelScope.launch { runCatching { repo.addPronunciation(sc.card.id, now, "android-recognizer", results.firstOrNull().orEmpty(), score) } }
        val done = score >= GradeInference.SPEECH_GOOD || attempts >= 3
        _ui.update { it.copy(heard = results.firstOrNull().orEmpty(), score = score, correct = if (done) score >= GradeInference.SPEECH_GOOD else null) }
        if (done) {
            val grade = GradeInference.speechGrade(score, attempts, ui.textShown)
            commit(sc, grade, now, hinted = ui.textShown)
        }
    }

    private fun commit(sc: StudyCard, grade: Grade, now: Long, hinted: Boolean = false, think: Long? = null, look: Long? = null) {
        graded += sc.card.id
        if (grade == Grade.AGAIN) mistakes += sc.card.id
        if (retry) { _ui.update { it.copy(mistakes = mistakes.size) }; return }
        val index = order.indexOfFirst { it.card.id == sc.card.id }.coerceAtLeast(0)
        viewModelScope.launch {
            try {
                val prev = sc.card
                val st = Srs.next(SrsState(prev.ease, prev.interval, prev.reps, prev.due), grade, now, limits.paceOf(prev.deckId), Random.nextDouble(-1.0, 1.0))
                val rid = repo.addReview(Review(prev.id, now, grade, think, look, prev.isNew, hinted, plays))
                repo.updateCardSrs(prev.id, st)
                val idx = order.indexOfFirst { it.card.id == prev.id }
                val updated = prev.copy(due = st.due, interval = st.interval, ease = st.ease, reps = st.reps)
                if (idx >= 0) order[idx] = sc.copy(card = updated)
                undo = Undo(index, sc, prev, rid)
                plays = 0
                _ui.update { it.copy(mistakes = mistakes.size, canUndo = true) }
            } catch (e: Exception) {
                _ui.update { it.copy(error = e.message) }
            }
        }
    }

    // ---------- переходы ----------
    /** Вперёд: оценка по поведению (для переворота), оценённая карточка уходит из очереди. */
    fun next() {
        val ui = _ui.value
        val sc = ui.current ?: return
        val task = ui.task
        val now = System.currentTimeMillis()
        if (sc.card.id !in graded && task?.kind == TaskKind.FLIP) {
            val sig = signals(now)
            val g = GradeInference.infer(sig, sc.card.reps)
            if (g != null) commit(sc, g, now, think = sig.think, look = sig.look)
        }
        if (sc.card.id in graded) {
            val idx = order.indexOfFirst { it.card.id == sc.card.id }
            if (idx >= 0) order.removeAt(idx)
            pos = if (pos >= order.size) 0 else pos
        } else if (order.isNotEmpty()) {
            pos = (pos + 1) % order.size
        }
        begin()
    }

    /** Назад: без оценки. */
    fun prev() {
        if (order.isEmpty()) return
        pos = (pos - 1 + order.size) % order.size
        begin()
    }

    /** ↶ отмена последней оценки: расписание и история возвращаются, карточка снова в очереди. */
    fun undoLast() {
        val u = undo ?: return
        undo = null
        viewModelScope.launch {
            try {
                if (!retry) { repo.restoreCardSrs(u.prev); repo.deleteReview(u.reviewId) }
                graded -= u.card.card.id; mistakes -= u.card.card.id
                val existing = order.indexOfFirst { it.card.id == u.card.card.id }
                if (existing >= 0) order[existing] = u.card.copy(card = u.prev)
                else order.add(u.index.coerceIn(0, order.size), u.card.copy(card = u.prev))
                tasks.remove(u.card.card.id)
                pos = order.indexOfFirst { it.card.id == u.card.card.id }.coerceAtLeast(0)
                begin()
            } catch (e: Exception) {
                _ui.update { it.copy(error = e.message) }
            }
        }
    }

    fun stopAudio() = tts.stop()
    override fun onCleared() { tts.stop() }
}
