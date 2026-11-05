package com.lingvadesk.app.presentation.common.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LingvaButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(50.dp),
        shape = RoundedCornerShape(12.dp),
        enabled = enabled
    ) {
        Text(text = text, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
fun LingvaTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true
    )
}

@Composable
fun CardFaceContainer(
    frontText: String,
    backText: String?,
    isFlipped: Boolean,
    onFlip: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onFlip,
        modifier = modifier.fillMaxWidth().height(260.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (!isFlipped) frontText else (backText ?: ""),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
