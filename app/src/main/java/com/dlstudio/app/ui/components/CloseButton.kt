package com.dlstudio.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.dlstudio.app.R

/** Кнопка-крестик «закрыть» (выход из сессии). */
@Composable
fun CloseButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Surface(modifier.size(44.dp).clickable(role = Role.Button, onClick = onClick), RoundedCornerShape(14.dp), colors.surfaceVariant) {
        Box(contentAlignment = Alignment.Center) { AppIcon(R.drawable.ic_close, colors.onBackground, 24.dp) }
    }
}
