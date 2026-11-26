package com.dlstudio.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dlstudio.app.content.QuizKind
import com.dlstudio.app.speech.Characters
import com.dlstudio.app.ui.theme.LocalAccent
import com.dlstudio.app.ui.theme.LocalStrings
import com.dlstudio.app.ui.theme.gradientColors

/** Значок вида вопроса в шапке карточки практики. */
fun kindIcon(kind: QuizKind, dictation: Boolean = false): String = when {
    dictation -> "🎧"
    kind == QuizKind.CHOICE -> "🎯"
    kind == QuizKind.LISTEN -> "🎧"
    kind == QuizKind.SPEAK -> "🎤"
    kind == QuizKind.BANK -> "🧩"
    kind == QuizKind.FILL -> "✍️"
    kind == QuizKind.GAP -> "🧱"
    kind == QuizKind.FIND -> "🔎"
    else -> "🔗"
}

/**
 * Карточка практики в том же стиле, что карточка теории: свечение, скруглённые углы, яркая шапка со значком и названием задания;
 * `aside` — то, что справа в шапке (например портрет говорящего). Содержимое задания идёт в теле карточки.
 */
@Composable
fun QuestionShell(icon: String, title: String, modifier: Modifier = Modifier, aside: (@Composable () -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(28.dp)
    Column(
        modifier.fillMaxWidth().shadow(14.dp, shape, ambientColor = colors.primary, spotColor = colors.primary).clip(shape)
            .background(colors.surfaceVariant).border(1.dp, colors.primary.copy(alpha = 0.35f), shape)
    ) {
        Row(
            Modifier.fillMaxWidth().background(Brush.linearGradient(LocalAccent.current.gradientColors())).padding(horizontal = 16.dp, vertical = 12.dp),
            Arrangement.spacedBy(12.dp), Alignment.CenterVertically
        ) {
            Box(Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = 0.22f)), Alignment.Center) { Text(icon, fontSize = 22.sp) }
            Text(title, Modifier.weight(1f), Color.White, style = MaterialTheme.typography.titleMedium)
            aside?.invoke()
        }
        Column(Modifier.fillMaxWidth().padding(18.dp), Arrangement.spacedBy(14.dp), content = content)
    }
}

/** Верх практики: номер вопроса, полоса прогресса и число верных ответов. */
@Composable
fun QuizHeader(pos: Int, total: Int, correct: Int) {
    Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(12.dp), Alignment.CenterVertically) {
        Pill("${pos + 1} / $total", accent = true)
        Box(Modifier.weight(1f)) { ProgressBar(pos / total.toFloat()) }
        Pill("✓ $correct")
    }
}

/** Реакция на ответ: персонаж, вердикт цветом ответа (верно — акцент, неверно — красный) и кнопка «Дальше». */
@Composable
fun FeedbackPanel(ok: Boolean, who: String, verdict: String, nextLabel: String, onNext: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val tone = if (ok) colors.primary else colors.error
    val shape = RoundedCornerShape(24.dp)
    Column(
        Modifier.fillMaxWidth().clip(shape).background(tone.copy(alpha = 0.14f)).border(1.5.dp, tone.copy(alpha = 0.6f), shape).padding(14.dp),
        Arrangement.spacedBy(12.dp)
    ) {
        Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(12.dp), Alignment.CenterVertically) {
            CharacterAvatar(who, 64.dp, mood = if (ok) Mood.HAPPY else Mood.SAD)
            Text((if (ok) "✓ " else "✗ ") + verdict, Modifier.weight(1f), tone, style = MaterialTheme.typography.titleMedium)
        }
        GradientButton(nextLabel, onNext)
    }
}

/** Итог практики: кольцо с долей верных ответов вокруг мисс Грейс (радуется, если этап пройден), счёт и «Пройти снова». */
@Composable
fun QuizResult(correct: Int, total: Int, good: Boolean, done: Boolean, onRetry: () -> Unit) {
    val s = LocalStrings.current
    val fraction = if (total == 0) 0f else correct / total.toFloat()
    QuestionShell("🏁", s.resultHeadline(good)) {
        Box(Modifier.fillMaxWidth(), Alignment.Center) {
            Ring(fraction, 190.dp, 12.dp) { CharacterAvatar(Characters.DEFAULT, 140.dp, mood = if (good) Mood.HAPPY else Mood.SAD) }
        }
        Text("${(fraction * 100).toInt()}%", Modifier.fillMaxWidth(), style = MaterialTheme.typography.headlineLarge, textAlign = TextAlign.Center)
        Text(s.quizResult(correct, total), Modifier.fillMaxWidth(), MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        if (done) Text(s.stageDone, Modifier.fillMaxWidth(), MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        GradientButton(s.retry, onRetry, Modifier.padding(top = 4.dp))
    }
}
