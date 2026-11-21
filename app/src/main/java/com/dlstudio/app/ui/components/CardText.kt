package com.dlstudio.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.dlstudio.app.settings.FontGroup
import com.dlstudio.app.ui.theme.TextRole
import com.dlstudio.app.ui.theme.inFont
import com.dlstudio.app.ui.theme.style

@Composable
fun FitText(text: String, style: TextStyle, modifier: Modifier = Modifier, color: Color = Color.Unspecified, maxLines: Int = 1) {
    Text(text, modifier, color, style = style, maxLines = maxLines)
}

@Composable
fun ButtonLabel(text: String, color: Color, modifier: Modifier = Modifier, role: TextRole = TextRole.BUTTON, maxLines: Int = 2) =
    FitText(text.capFirst(), role.style(), modifier, color, maxLines)

fun String.capFirst(): String = replaceFirstChar { it.titlecase() }

@Composable
fun GroupText(text: String, group: FontGroup, modifier: Modifier = Modifier, color: Color = Color.Unspecified, style: TextStyle = MaterialTheme.typography.bodyLarge) {
    Text(text, modifier, color, style = style.inFont(group))
}

@Composable
fun StatusText(text: String, busy: Boolean) {
    Column {
        if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
        Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun ScreenHeader(text: String) {
    FitText(text, MaterialTheme.typography.headlineMedium, Modifier.fillMaxWidth().padding(bottom = 16.dp))
}
