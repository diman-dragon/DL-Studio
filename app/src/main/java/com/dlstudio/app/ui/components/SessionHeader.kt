package com.dlstudio.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.dlstudio.app.R
import com.dlstudio.app.ui.theme.LocalStrings

/** Верхняя строка сессии: выход, полоса прогресса и счётчик «3 из 20». */
@Composable
fun SessionHeader(finished: Int, total: Int, onClose: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(14.dp), Alignment.CenterVertically) {
        Surface(Modifier.size(44.dp).clickable(role = Role.Button, onClick = onClose), RoundedCornerShape(14.dp), colors.surfaceVariant) {
            Box(contentAlignment = Alignment.Center) { AppIcon(R.drawable.ic_back, colors.onBackground, 26.dp) }
        }
        Box(Modifier.weight(1f).height(10.dp).clip(RoundedCornerShape(5.dp)).background(colors.surfaceVariant)) {
            Box(Modifier.fillMaxHeight().fillMaxWidth(if (total == 0) 0f else (finished.toFloat() / total).coerceIn(0f, 1f)).clip(RoundedCornerShape(5.dp)).background(colors.primary))
        }
        Text(LocalStrings.current.sessionProgress(finished, total), style = MaterialTheme.typography.labelLarge, color = colors.onSurfaceVariant)
    }
}
