package com.dlstudio.app.ui.topic

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dlstudio.app.content.Answers
import com.dlstudio.app.content.QuizItem
import com.dlstudio.app.ui.components.GradientButton
import com.dlstudio.app.ui.components.QuestionShell
import com.dlstudio.app.ui.components.kindIcon
import com.dlstudio.app.ui.theme.LocalStrings

private val SLOT_WIDTH = 84.dp
private val SLOT_HEIGHT = 44.dp

/**
 * «Вставь слова»: предложение с пропусками `___`, внизу слова (есть и лишние). Нажатие по слову ставит его в первый свободный пропуск,
 * нажатие по заполненному пропуску возвращает слово обратно. Когда заполнены все пропуски, кнопка «Проверить».
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GapQuestion(item: QuizItem, onDone: (Boolean) -> Unit) {
    val s = LocalStrings.current
    val colors = MaterialTheme.colorScheme
    val parts = remember(item) { Answers.gapParts(item) }
    val bag = remember(item) { Answers.gapBag(item) }
    var slots by remember(item) { mutableStateOf(List<Int?>(parts.size - 1) { null }) }
    var result by remember(item) { mutableStateOf<Boolean?>(null) }
    val open = result == null
    Column(Modifier.fillMaxWidth(), Arrangement.spacedBy(14.dp)) {
        QuestionShell(kindIcon(item.kind), s.quizKind(item.kind, false)) {
            FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                parts.forEachIndexed { i, part ->
                    part.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }.forEach { Text(it, style = MaterialTheme.typography.headlineSmall) }
                    if (i < slots.size) {
                        val at = slots[i]
                        GapSlot(at?.let { bag[it] }, result, open && at != null) { slots = slots.toMutableList().also { list -> list[i] = null } }
                    }
                }
            }
            if (item.hint.isNotBlank()) Text("💡 ${item.hint}", style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
        }
        FlowRow(Modifier.fillMaxWidth().padding(top = 4.dp), Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally), Arrangement.spacedBy(8.dp)) {
            bag.indices.forEach { index ->
                val used = index in slots
                WordChip(bag[index], ghost = used, enabled = open && !used && slots.any { it == null }) {
                    val free = slots.indexOfFirst { it == null }
                    if (free >= 0) slots = slots.toMutableList().also { list -> list[free] = index }
                }
            }
        }
        if (open && slots.none { it == null }) GradientButton(s.check, {
            val ok = Answers.gapOk(item, slots.map { bag[it!!] })
            result = ok
            onDone(ok)
        })
    }
}

/** Пропуск в предложении: пустой — рамка-подчёркивание, заполненный — слово (нажатие возвращает его в мешок); после проверки обводка цветом результата. */
@Composable
private fun GapSlot(word: String?, result: Boolean?, removable: Boolean, onRemove: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(12.dp)
    val line = when (result) { true -> colors.primary; false -> colors.error; null -> if (word == null) colors.outline else colors.primary.copy(alpha = 0.6f) }
    Box(
        Modifier.widthIn(min = SLOT_WIDTH).heightIn(min = SLOT_HEIGHT).clip(shape).background(if (word == null) colors.surfaceContainerLow else colors.primaryContainer)
            .border(1.5.dp, line, shape).clickable(enabled = removable, role = Role.Button, onClick = onRemove).padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) { Text(word.orEmpty(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
}

/**
 * «Найди ошибку»: предложение разбито на слова-кнопки; нужно нажать на слово с ошибкой. Ответ принимается сразу: выбранное слово
 * подсвечивается верным или неверным цветом, а при промахе обводкой показывается настоящая ошибка.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FindQuestion(item: QuizItem, onDone: (Boolean) -> Unit) {
    val s = LocalStrings.current
    val colors = MaterialTheme.colorScheme
    val words = remember(item) { Answers.findWords(item) }
    val target = remember(item) { Answers.findIndex(item) }
    var picked by remember(item) { mutableStateOf<Int?>(null) }
    val shape = RoundedCornerShape(14.dp)
    Column(Modifier.fillMaxWidth(), Arrangement.spacedBy(14.dp)) {
        QuestionShell(kindIcon(item.kind), s.quizKind(item.kind, false)) {
            Text(s.findPrompt, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            FlowRow(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp), Arrangement.spacedBy(8.dp)) {
                words.forEachIndexed { i, word ->
                    val line = when {
                        picked == null -> colors.outlineVariant
                        i == target -> colors.primary
                        i == picked -> colors.error
                        else -> colors.outlineVariant
                    }
                    val fill = when { picked != null && i == target -> colors.primaryContainer; picked == i -> colors.errorContainer; else -> colors.surfaceContainerHigh }
                    Box(
                        Modifier.clip(shape).background(fill).border(1.5.dp, line, shape)
                            .clickable(enabled = picked == null, role = Role.Button) { picked = i; onDone(i == target) }.padding(horizontal = 12.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) { Text(word, style = MaterialTheme.typography.titleLarge) }
                }
            }
        }
    }
}
