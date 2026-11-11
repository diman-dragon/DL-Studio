package com.dlstudio.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.dlstudio.app.ui.theme.LocalStrings

@Composable
fun ExpandableNote(text: String, modifier: Modifier = Modifier) {
    var expanded by rememberSaveable(text) { mutableStateOf(false) }
    val strings = LocalStrings.current
    CardNote(text, modifier.clickable(role = Role.Button) { expanded = true })
    if (expanded) AlertDialog(
        onDismissRequest = { expanded = false },
        title = { Text(strings.cardExplanation) },
        text = { Text(text, Modifier.heightIn(max = 320.dp).verticalScroll(rememberScrollState()), style = MaterialTheme.typography.bodyLarge) },
        confirmButton = { TextButton(onClick = { expanded = false }) { Text(strings.close) } }
    )
}
