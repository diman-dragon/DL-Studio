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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.dlstudio.app.ui.theme.LocalStrings
import androidx.compose.ui.unit.dp
import com.dlstudio.app.R

/** Компонентная кнопка «Назад» со стрелкой возврата. */
@Composable
fun BackButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val label = LocalStrings.current.previous
    Surface(
        modifier = modifier.size(48.dp).clickable(role = Role.Button, onClick = onClick).semantics { contentDescription = label },
        shape = RoundedCornerShape(14.dp),
        color = colors.surfaceVariant
    ) {
        Box(contentAlignment = Alignment.Center) { AppIcon(R.drawable.ic_back, colors.onBackground, 26.dp) }
    }
}
