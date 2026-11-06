package com.dlstudio.app.ui.components

import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dlstudio.app.ui.theme.LocalStrings

@Composable
fun VoicePicker(voices: List<Voice>, selected: String?, tts: TextToSpeech?, onSelect: (Voice) -> Unit, onClose: () -> Unit) {
    val s = LocalStrings.current
    DlModal.Shell(s.speechVoice, onClose) {
        Column(Modifier.heightIn(max = 390.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (voices.isEmpty()) Text(s.voicePackagesEmpty, color = MaterialTheme.colorScheme.onSurfaceVariant)
            voices.sortedWith(compareBy({ it.locale.displayName }, { it.name })).forEach { voice ->
                val language = voice.locale.getDisplayName(voice.locale).replaceFirstChar { it.titlecase(voice.locale) }
                Surface(Modifier.fillMaxWidth().clickable { onSelect(voice) }, RoundedCornerShape(16.dp), color = if (voice.name == selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant) {
                    Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) { Column(Modifier.weight(1f)) { Text(language); Text(voice.name.replace('_', ' '), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }; DlModal.Action("▶", { tts?.setVoice(voice); tts?.speak(s.voicePreview, TextToSpeech.QUEUE_FLUSH, null, "preview") }) }
                }
            }
        }
    }
}
