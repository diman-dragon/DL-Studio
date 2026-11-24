package com.dlstudio.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dlstudio.app.speech.Characters
import com.dlstudio.app.speech.Speaking
import com.dlstudio.app.ui.theme.LocalStrings
import kotlinx.coroutines.delay
import kotlin.random.Random

/** Кто и что сейчас говорит вслух (null — тишина). Аватары по нему оживают: двигается тот, чей голос звучит. */
val LocalSpeaking = compositionLocalOf<Speaking?> { null }

/** Настроение персонажа: обычное, радуется верному ответу, огорчается неверному. */
enum class Mood { NEUTRAL, HAPPY, SAD }

private const val TALK_MS = 130L
private const val BLINK_MS = 120L
private const val BLINK_MIN_MS = 2400L
private const val BLINK_MAX_MS = 4800L
private const val BOB_PERIOD = 1800
private val TALK_FRAMES = listOf("talk1", "talk2", "idle", "talk1", "talk2", "talk1")

/** Говорит ли сейчас персонаж `id`; если задана метка строки `line` — именно эта строка (одинаковые реплики разных строк не путаются). */
fun isSpeaking(speaking: Speaking?, id: String, line: String? = null): Boolean =
    speaking != null && speaking.characterId == id && (line == null || speaking.line == line)

/**
 * Портрет персонажа (открытый набор Micah, CC BY 4.0), живой: дышит, моргает, шевелит ртом, пока звучит его голос, радуется или огорчается.
 * Кадры — готовые картинки (`PortraitRes`); пока персонаж говорит, вокруг портрета светится кольцо цвета персонажа.
 * Если задана `line`, оживает только та строка, которую озвучивают (а не любая реплика этого персонажа). Нажатие (`onClick`) необязательно.
 */
@Composable
fun CharacterAvatar(id: String, size: Dp, modifier: Modifier = Modifier, line: String? = null, mood: Mood = Mood.NEUTRAL, onClick: (() -> Unit)? = null) {
    val profile = Characters.of(id)
    val talking = isSpeaking(LocalSpeaking.current, profile.id, line)
    val tint = Color(profile.tint)
    var frame by remember { mutableStateOf("idle") }
    LaunchedEffect(talking, mood) {
        when {
            talking -> { var i = 0; while (true) { frame = TALK_FRAMES[i++ % TALK_FRAMES.size]; delay(TALK_MS) } }
            mood == Mood.HAPPY -> frame = "happy"
            mood == Mood.SAD -> frame = "sad"
            else -> while (true) { frame = "idle"; delay(Random.nextLong(BLINK_MIN_MS, BLINK_MAX_MS)); frame = "blink"; delay(BLINK_MS) }
        }
    }
    val bob by rememberInfiniteTransition(label = "bob").animateFloat(-1f, 1f, infiniteRepeatable(tween(BOB_PERIOD, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "bob")
    val ring = if (talking) Modifier.border(3.dp, tint, CircleShape) else Modifier
    val tap = if (onClick == null) Modifier else Modifier.clickable(role = Role.Button, onClick = onClick)
    Box(
        modifier.size(size).clip(CircleShape).background(Brush.verticalGradient(listOf(tint.copy(alpha = 0.55f), tint))).then(ring).then(tap),
        Alignment.BottomCenter
    ) {
        PortraitRes.of(profile.id, frame)?.let { res ->
            Image(
                painterResource(res), LocalStrings.current.characterName(profile.id),
                Modifier.size(size).graphicsLayer { translationY = bob * this.size.height * 0.012f; scaleX = 1.06f; scaleY = 1.06f }, contentScale = ContentScale.Crop
            )
        }
    }
}
