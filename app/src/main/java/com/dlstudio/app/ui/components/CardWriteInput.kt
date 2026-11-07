package com.dlstudio.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.dlstudio.app.R
import com.dlstudio.app.ui.theme.LocalStrings

/** Поле ввода ответа для режимов письма с кнопкой проверки. */
@Composable
fun CardWriteInput(correct: String, submitted: Boolean, onAnswer: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    var text by remember { mutableStateOf("") }
    val isCorrect = text.trim().equals(correct.trim(), ignoreCase = true)
    val cs = MaterialTheme.colorScheme
    val okColor = Color(0xFF2E7D32); val errColor = Color(0xFFC62828)
    val btnColor = if (submitted) (if (isCorrect) okColor else errColor) else cs.primary
    fun submit() { if (!submitted && text.isNotBlank()) onAnswer(isCorrect) }

    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(10.dp), Alignment.CenterVertically) {
            OutlinedTextField(
                value = text, onValueChange = { if (!submitted) text = it }, modifier = Modifier.weight(1f),
                placeholder = { Text(LocalStrings.current.writePrompt, maxLines = 1) }, singleLine = true,
                enabled = !submitted, shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { submit() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (submitted) btnColor else cs.primary,
                    unfocusedBorderColor = if (submitted) btnColor else cs.outlineVariant
                )
            )
            Surface(Modifier.size(54.dp), RoundedCornerShape(16.dp), color = btnColor, onClick = { submit() }) {
                Box(Modifier.fillMaxSize(), Alignment.Center) { AppIcon(R.drawable.ic_check, Color.White, 26.dp) }
            }
        }
        if (submitted && !isCorrect) {
            Text(correct, style = MaterialTheme.typography.bodyMedium, color = okColor, modifier = Modifier.padding(horizontal = 4.dp))
        }
    }
}
