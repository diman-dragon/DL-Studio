package com.dlstudio.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dlstudio.app.R
import com.dlstudio.app.library.LibNode

@Composable
fun StudyFlashCard(card: LibNode, flipped: Boolean, hintShown: Boolean, onFlip: () -> Unit, onHint: () -> Unit, onSpeak: (String) -> Unit, modifier: Modifier = Modifier) {
    val turn by animateFloatAsState(if (flipped) 180f else 0f, spring(stiffness = Spring.StiffnessLow), label = "flip")
    val shape = RoundedCornerShape(28.dp)
    Box(modifier.fillMaxWidth().height(390.dp).graphicsLayer { rotationY = turn; cameraDistance = 18f * density }.clip(shape).background(MaterialTheme.colorScheme.surface).border(1.dp, MaterialTheme.colorScheme.outline, shape).clickable { onFlip() }) {
        Column(Modifier.fillMaxSize().graphicsLayer { if (flipped) rotationY = 180f }.padding(26.dp), Arrangement.SpaceBetween, Alignment.CenterHorizontally) {
            if (!flipped) {
                Text(card.name, Modifier.fillMaxWidth(), style = MaterialTheme.typography.displaySmall, textAlign = TextAlign.Center)
                Box(Modifier.fillMaxWidth().height(92.dp)) { if (hintShown) Text(card.hint, Modifier.align(Alignment.TopCenter), color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center); Row(Modifier.align(Alignment.BottomCenter), Arrangement.spacedBy(12.dp)) { if (card.hint.isNotBlank()) FixedIconAction(R.drawable.ic_lightbulb, onHint); FixedIconAction(R.drawable.ic_volume, { onSpeak(card.name) }) } }
            } else {
                Text(card.back.ifBlank { card.name }, Modifier.fillMaxWidth(), style = MaterialTheme.typography.displaySmall, textAlign = TextAlign.Center)
                Column(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) { if (card.explanation.isNotBlank()) Text(card.explanation, color = MaterialTheme.colorScheme.onSurfaceVariant); if (card.example.isNotBlank()) Text(card.example, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                Box(Modifier.fillMaxWidth().height(52.dp)) { FixedIconAction(R.drawable.ic_volume, { onSpeak(card.back) }, card.back.isNotBlank(), Modifier.align(Alignment.BottomCenter)) }
            }
        }
    }
}

