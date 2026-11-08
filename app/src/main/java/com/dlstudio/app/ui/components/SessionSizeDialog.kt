package com.dlstudio.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dlstudio.app.ui.theme.LocalStrings

/** Окно «Своё число карточек в сессии»: число от 1 до 500; пока оно неверное, «Сохранить» ничего не делает. */
@Composable
fun SessionSizeDialog(current: Int, onSave: (Int) -> Unit, onClose: () -> Unit) {
    val s = LocalStrings.current
    val colors = MaterialTheme.colorScheme
    val focus = remember { FocusRequester() }
    var text by remember { mutableStateOf(current.toString()) }
    val size = text.toIntOrNull()?.takeIf { it in 1..500 }
    LaunchedEffect(Unit) { focus.requestFocus() }
    DlModal.Shell(s.customSizeTitle, onClose) {
        Surface(Modifier.fillMaxWidth(), RoundedCornerShape(16.dp), colors.surfaceVariant, border = BorderStroke(1.5.dp, if (size == null) colors.error else colors.primary)) {
            Box(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), Alignment.Center) {
                BasicTextField(
                    text, { text = it.filter(Char::isDigit).take(3) }, Modifier.fillMaxWidth().focusRequester(focus), singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = MaterialTheme.typography.headlineMedium.copy(color = colors.onSurface, textAlign = TextAlign.Center),
                    cursorBrush = SolidColor(colors.primary)
                )
            }
        }
        Text(s.customSizeHint, color = if (size == null) colors.error else colors.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        DlModal.Actions(s.cancel, s.save, onClose, { size?.let(onSave) })
    }
}
