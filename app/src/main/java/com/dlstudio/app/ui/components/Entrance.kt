package com.dlstudio.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

private const val STAGGER_MS = 70L

/** Блок выезжает снизу и проявляется; `index` задаёт очередь (каждый следующий чуть позже). Играет один раз — при первом показе. */
@Composable
fun Reveal(index: Int, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val shown = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(index * STAGGER_MS)
        shown.animateTo(1f, tween(480, easing = FastOutSlowInEasing))
    }
    Box(modifier.fillMaxWidth().graphicsLayer { alpha = shown.value; translationY = (1f - shown.value) * 32.dp.toPx() }) { content() }
}

/** Число, которое «набегает» от нуля до `target` при показе и при каждом изменении. */
@Composable
fun rememberCountUp(target: Int, millis: Int = 900): Int {
    val value = remember { Animatable(0f) }
    LaunchedEffect(target) { value.animateTo(target.toFloat(), tween(millis, easing = FastOutSlowInEasing)) }
    return value.value.toInt()
}

/** Доля (кольцо, полоса), которая при первом показе вырастает от нуля, а потом плавно следует за `target`. */
@Composable
fun rememberGrow(target: Float, millis: Int = 900): Float {
    var goal by remember { mutableStateOf(0f) }
    LaunchedEffect(target) { goal = target }
    return animateFloatAsState(goal, tween(millis, easing = FastOutSlowInEasing), label = "grow").value
}
