package com.dlstudio.app.ui.topic

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dlstudio.app.content.Answers
import com.dlstudio.app.content.QuizItem
import com.dlstudio.app.ui.components.GradientButton
import com.dlstudio.app.ui.components.QuestionShell
import com.dlstudio.app.ui.components.kindIcon
import com.dlstudio.app.ui.theme.LocalStrings

private val SLOT_HEIGHT = 72.dp

/**
 * «Мешок слов»: из слов внизу нужно собрать ответ. Нажатие переносит слово в строку ответа, нажатие по слову в строке возвращает его в мешок.
 * В мешке могут быть лишние слова. Проверяет кнопка «Проверить».
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BankQuestion(item: QuizItem, onDone: (Boolean) -> Unit) {
    val s = LocalStrings.current
    val colors = MaterialTheme.colorScheme
    val bag = remember(item) { Answers.bag(item) }
    var placed by remember(item) { mutableStateOf(listOf<Int>()) }
    var result by remember(item) { mutableStateOf<Boolean?>(null) }
    val open = result == null
    Column(Modifier.fillMaxWidth(), Arrangement.spacedBy(14.dp)) {
        QuestionShell(kindIcon(item.kind), s.quizKind(item.kind, false)) {
            Text(item.question, style = MaterialTheme.typography.headlineSmall)
            if (item.hint.isNotBlank()) Text("💡 ${item.hint}", style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
        }
        val shape = RoundedCornerShape(18.dp)
        val line = when (result) { true -> colors.primary; false -> colors.error; null -> colors.outline }
        FlowRow(
            Modifier.fillMaxWidth().heightIn(min = SLOT_HEIGHT).clip(shape).background(colors.surfaceContainerLow).border(1.5.dp, line, shape).padding(10.dp),
            Arrangement.spacedBy(8.dp), Arrangement.spacedBy(8.dp)
        ) {
            if (placed.isEmpty()) Text(s.bankPlaceholder, Modifier.padding(6.dp), style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            placed.forEach { index -> WordChip(bag[index], ghost = false, enabled = open) { placed = placed - index } }
        }
        FlowRow(Modifier.fillMaxWidth().padding(top = 4.dp), Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally), Arrangement.spacedBy(8.dp)) {
            bag.indices.forEach { index ->
                val used = index in placed
                WordChip(bag[index], ghost = used, enabled = open && !used) { placed = placed + index }
            }
        }
        if (open && placed.isNotEmpty()) GradientButton(s.check, {
            val ok = Answers.bankOk(item, placed.map { bag[it] })
            result = ok
            onDone(ok)
        })
    }
}

@Composable
private fun WordChip(text: String, ghost: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(14.dp)
    Box(
        Modifier.alpha(if (ghost) 0.25f else 1f).clip(shape).background(colors.surfaceContainerHigh).border(1.dp, colors.outlineVariant, shape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick).padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) { Text(text, style = MaterialTheme.typography.titleMedium) }
}

/** Состояние плитки в «соедини пары». */
private enum class PairMark { IDLE, PICKED, DONE, WRONG }

/**
 * «Соедини пары»: две колонки. Нажмите любой вариант — слева или справа, затем его пару с другой стороны: пара закрепится, если подошла. Допускается одна ошибка (если пар не меньше четырёх).
 */
@Composable
fun MatchQuestion(item: QuizItem, onDone: (Boolean) -> Unit) {
    val s = LocalStrings.current
    val order = remember(item) { Answers.matchOrder(item) }
    val leftAt = order.first
    val rightAt = order.second
    var pickedLeft by remember(item) { mutableStateOf<Int?>(null) }
    var pickedRight by remember(item) { mutableStateOf<Int?>(null) }
    var leftDone by remember(item) { mutableStateOf(setOf<Int>()) }
    var rightDone by remember(item) { mutableStateOf(setOf<Int>()) }
    var wrong by remember(item) { mutableStateOf<Pair<Int, Int>?>(null) }
    var mistakes by remember(item) { mutableStateOf(0) }

    /** Проверяет пару «слева i, справа j»; начинать можно с любой стороны. */
    fun check(i: Int, j: Int) {
        if (item.pairs[leftAt[i]].second == item.pairs[rightAt[j]].second) {
            leftDone = leftDone + i
            rightDone = rightDone + j
            wrong = null
            if (leftDone.size == item.pairs.size) onDone(Answers.matchOk(item.pairs.size, mistakes))
        } else {
            mistakes += 1
            wrong = i to j
        }
        pickedLeft = null
        pickedRight = null
    }

    fun tapLeft(i: Int) {
        wrong = null
        val j = pickedRight
        if (j != null) check(i, j) else pickedLeft = if (pickedLeft == i) null else i
    }

    fun tapRight(j: Int) {
        wrong = null
        val i = pickedLeft
        if (i != null) check(i, j) else pickedRight = if (pickedRight == j) null else j
    }

    Column(Modifier.fillMaxWidth(), Arrangement.spacedBy(14.dp)) {
        QuestionShell(kindIcon(item.kind), s.quizKind(item.kind, false)) {
            Text(item.question.ifBlank { s.matchPrompt }, style = MaterialTheme.typography.headlineSmall)
            Text(s.matchHint, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f), Arrangement.spacedBy(10.dp)) {
                leftAt.forEachIndexed { i, at ->
                    val mark = when { i in leftDone -> PairMark.DONE; wrong?.first == i -> PairMark.WRONG; pickedLeft == i -> PairMark.PICKED; else -> PairMark.IDLE }
                    PairChip(item.pairs[at].first, mark) { tapLeft(i) }
                }
            }
            Column(Modifier.weight(1f), Arrangement.spacedBy(10.dp)) {
                rightAt.forEachIndexed { j, at ->
                    val mark = when { j in rightDone -> PairMark.DONE; wrong?.second == j -> PairMark.WRONG; pickedRight == j -> PairMark.PICKED; else -> PairMark.IDLE }
                    PairChip(item.pairs[at].second, mark) { tapRight(j) }
                }
            }
        }
    }
}

@Composable
private fun PairChip(text: String, mark: PairMark, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(16.dp)
    val (fill, line) = when (mark) {
        PairMark.IDLE -> colors.surfaceContainerHigh to colors.outlineVariant
        PairMark.PICKED -> colors.primary.copy(alpha = 0.22f) to colors.primary
        PairMark.DONE -> colors.primary.copy(alpha = 0.10f) to colors.primary.copy(alpha = 0.4f)
        PairMark.WRONG -> colors.error.copy(alpha = 0.2f) to colors.error
    }
    Box(
        Modifier.fillMaxWidth().heightIn(min = 60.dp).alpha(if (mark == PairMark.DONE) 0.55f else 1f).clip(shape).background(fill).border(1.5.dp, line, shape)
            .clickable(enabled = mark != PairMark.DONE, role = Role.Button, onClick = onClick).padding(horizontal = 10.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) { Text(text, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center) }
}
