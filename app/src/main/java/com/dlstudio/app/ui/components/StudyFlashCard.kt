package com.dlstudio.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dlstudio.app.R
import com.dlstudio.app.library.LibNode

@Composable
fun StudyFlashCard(card: LibNode, flipped: Boolean, hintShown: Boolean, onFlip: () -> Unit, onHint: () -> Unit, onSpeak: (String) -> Unit, modifier: Modifier = Modifier) {
    val turn by animateFloatAsState(if (flipped) 180f else 0f, tween(360, easing = FastOutSlowInEasing), label = "flip")
    val flip by rememberUpdatedState(onFlip)
    val showBack = turn > 90f
    val shape = RoundedCornerShape(28.dp)
    Box(modifier.fillMaxWidth().height(390.dp).graphicsLayer { rotationY = turn; cameraDistance = 24f * density }.clip(shape).background(MaterialTheme.colorScheme.surface).border(1.dp, MaterialTheme.colorScheme.outline, shape).pointerInput(Unit) { awaitEachGesture { val down = awaitFirstDown(requireUnconsumed = false); var moved = down.isConsumed; while (true) { val change = awaitPointerEvent().changes.firstOrNull { it.id == down.id } ?: break; if (change.isConsumed || (change.position - down.position).getDistance() > viewConfiguration.touchSlop) moved = true; if (!change.pressed) break }; if (!moved) flip() } }) {
        Column(Modifier.fillMaxSize().graphicsLayer { if (showBack) rotationY = 180f }.padding(26.dp), Arrangement.SpaceBetween, Alignment.CenterHorizontally) {
            if (!showBack) {
                Text(card.name, Modifier.fillMaxWidth(), style = MaterialTheme.typography.displaySmall, textAlign = TextAlign.Center)
                Box(Modifier.fillMaxWidth().height(100.dp)) { if (hintShown && card.hint.isNotBlank()) Text(card.hint, Modifier.align(Alignment.TopCenter), color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center); Row(Modifier.align(Alignment.BottomCenter), Arrangement.spacedBy(14.dp)) { FixedIconAction(R.drawable.ic_lightbulb, onHint, card.hint.isNotBlank(), iconTint = MaterialTheme.colorScheme.tertiary, containerColor = MaterialTheme.colorScheme.tertiaryContainer); FixedIconAction(R.drawable.ic_volume, { onSpeak(card.name) }, iconTint = MaterialTheme.colorScheme.primary, containerColor = MaterialTheme.colorScheme.primaryContainer) } }
            } else {
                Text(card.back.ifBlank { card.name }, Modifier.fillMaxWidth(), style = MaterialTheme.typography.displaySmall, textAlign = TextAlign.Center)
                Column(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) { if (card.explanation.isNotBlank()) Text(card.explanation, color = MaterialTheme.colorScheme.onSurfaceVariant); if (card.example.isNotBlank()) Text(card.example, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                Box(Modifier.fillMaxWidth().height(60.dp)) { FixedIconAction(R.drawable.ic_volume, { onSpeak(card.back) }, card.back.isNotBlank(), Modifier.align(Alignment.BottomCenter), MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer) }
            }
        }
    }
}

