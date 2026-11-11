package com.dlstudio.app.ui.components
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import kotlinx.coroutines.delay
@Composable
fun StudyTaskPopup(text: String, key: String) {
    var visible by remember(key) { mutableStateOf(false) }
    LaunchedEffect(key) {
        visible = true
        delay(1700)
        visible = false
    }
    Popup(alignment = Alignment.TopCenter, offset = IntOffset(0, -58), properties = PopupProperties(clippingEnabled = false)) {
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(initialOffsetY = { it / 2 }, animationSpec = tween(240)) + fadeIn(tween(240)),
            exit = slideOutVertically(targetOffsetY = { -it / 2 }, animationSpec = tween(240)) + fadeOut(tween(240))
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(.94f),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                tonalElevation = 6.dp
            ) {
                Text(
                    text.capFirst(),
                    Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.titleLarge.copy(textAlign = TextAlign.Center),
                    maxLines = 2
                )
            }
        }
    }
}
