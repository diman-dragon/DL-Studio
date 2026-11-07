package com.dlstudio.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dlstudio.app.R
import com.dlstudio.app.ui.theme.LocalStrings

/** Блок ввода ответа с авто-раскладкой, кнопкой проверки и наглядной реакцией 👍 / 👎. */
@Composable
fun CardWriteInput(correct: String, submitted: Boolean, onAnswer: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    var text by remember { mutableStateOf("") }
    val isCorrect = text.trim().equals(correct.trim(), ignoreCase = true)
    val cs = MaterialTheme.colorScheme
    val ok = Color(0xFF2E7D32); val err = Color(0xFFC62828)
    val btnColor = if (submitted) (if (isCorrect) ok else err) else cs.primary
    val isRu = correct.any { it in 'а'..'я' || it in 'А'..'Я' || it == 'ё' || it == 'Ё' }
    val borderColor = if (submitted) btnColor else cs.outlineVariant
    fun submit() { if (!submitted && text.isNotBlank()) onAnswer(isCorrect) }

    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(10.dp), Alignment.CenterVertically) {
            CardWriteField(
                value = text, onValueChange = { if (!submitted) text = it },
                placeholder = LocalStrings.current.writePrompt, isRussian = isRu,
                enabled = !submitted, borderColor = borderColor, onDone = ::submit,
                modifier = Modifier.weight(1f)
            )
            ActionIconButton(R.drawable.ic_check, ::submit, enabled = !submitted && text.isNotBlank(), size = 54.dp, shape = RoundedCornerShape(16.dp), containerColor = btnColor, iconTint = Color.White)
        }
        if (submitted) Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(if (isCorrect) ok.copy(alpha = .16f) else err.copy(alpha = .16f)).padding(horizontal = 14.dp, vertical = 8.dp),
            Arrangement.spacedBy(10.dp), Alignment.CenterVertically
        ) {
            Text(if (isCorrect) "👍" else "👎", fontSize = 24.sp)
            Text(if (isCorrect) "Правильно!" else "Неправильно: $correct", style = MaterialTheme.typography.titleMedium, color = if (isCorrect) ok else err)
        }
    }
}
