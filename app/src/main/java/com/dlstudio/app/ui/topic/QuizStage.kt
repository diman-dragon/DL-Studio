package com.dlstudio.app.ui.topic

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dlstudio.app.content.QuizItem
import com.dlstudio.app.ui.components.ChoiceOptions
import com.dlstudio.app.ui.components.GradientButton
import com.dlstudio.app.ui.components.ProgressBar
import com.dlstudio.app.ui.components.SettingCard
import com.dlstudio.app.ui.theme.LocalStrings

/**
 * Практика: вопросы по порядку с готовыми вариантами. Верный ответ даёт XP (один раз за вопрос), неверный просто показывает правильный.
 * В конце — результат и «Пройти снова». Этап считается пройденным, когда верно решено 60% вопросов (`done` приходит снаружи).
 */
@Composable
fun QuizStage(items: List<QuizItem>, done: Boolean, onAnswer: (Int, Boolean) -> Unit, modifier: Modifier = Modifier) {
    val s = LocalStrings.current
    var index by rememberSaveable { mutableStateOf(0) }
    var picked by rememberSaveable(index) { mutableStateOf<String?>(null) }
    var correct by rememberSaveable { mutableStateOf(0) }
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), Arrangement.spacedBy(16.dp)) {
        if (index >= items.size) {
            Text(s.quizResult(correct, items.size), Modifier.fillMaxWidth().padding(top = 24.dp), style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
            if (done) Text(s.stageDone, Modifier.fillMaxWidth(), MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
            GradientButton(s.retry, { index = 0; correct = 0 })
        } else {
            val item = items[index]
            ProgressBar(index / items.size.toFloat())
            SettingCard { Text(item.question, style = MaterialTheme.typography.headlineSmall) }
            ChoiceOptions(item.options, item.answer, picked, { choice ->
                picked = choice
                val ok = choice == item.answer
                if (ok) correct += 1
                onAnswer(index, ok)
            })
            if (picked != null) {
                Text(if (picked == item.answer) s.correct else s.wrongAnswer(item.answer), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                GradientButton(if (index == items.lastIndex) s.seeResult else s.next, { index += 1 })
            }
        }
    }
}
