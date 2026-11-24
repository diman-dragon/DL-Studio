package com.dlstudio.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dlstudio.app.speech.Gender
import com.dlstudio.app.speech.Speaking

/** Кто и что сейчас говорит вслух (null — тишина). Аватары преподавателей по нему шевелят ртом. */
val LocalSpeaking = compositionLocalOf<Speaking?> { null }

/** Настроение преподавателя: обычное, радуется верному ответу, огорчается неверному. */
enum class Mood { NEUTRAL, HAPPY, SAD }

private val SKIN = Color(0xFFF2C9A5)
private val INK = Color(0xFF2B2B33)
private val MOUTH = Color(0xFF8B2E3A)
private const val BLINK_PERIOD = 3600
private const val BOB_PERIOD = 1800
private const val MOUTH_PERIOD = 170

/**
 * Преподаватель, нарисованный кодом и живой: дышит, моргает, шевелит ртом, пока говорит, и меняет настроение.
 * Gender.F — мисс Грейс: строгая учительница, пучок, очки, тёмный деловой костюм с белым воротничком.
 * Gender.M — мистер Лео: молодой энтузиаст, взъерошенная чёлка, борода, горчичный свитер, красная бабочка.
 * Говорит, когда вслух звучит его голос (если задан `text` — именно эта реплика). Нажатие (`onClick`) необязательно.
 */
@Composable
fun TeacherAvatar(gender: Int, size: Dp, modifier: Modifier = Modifier, text: String? = null, mood: Mood = Mood.NEUTRAL, speakerId: String? = null, onClick: (() -> Unit)? = null) {
    val speaking = LocalSpeaking.current
    val expectedId = speakerId ?: if (gender == Gender.F) "grace" else "leo"
    val talking = speaking != null && speaking.characterId == expectedId && (text == null || speaking.text == text)
    val clock = rememberInfiniteTransition(label = "teacher")
    val blink by clock.animateFloat(
        0f, 1f,
        infiniteRepeatable(keyframes { durationMillis = BLINK_PERIOD; 0f at 0; 0f at 3300; 1f at 3400; 0f at 3500; 0f at BLINK_PERIOD }), label = "blink"
    )
    val bob by clock.animateFloat(-1f, 1f, infiniteRepeatable(tween(BOB_PERIOD, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "bob")
    val mouth by clock.animateFloat(0f, 1f, infiniteRepeatable(tween(MOUTH_PERIOD, easing = LinearEasing), RepeatMode.Reverse), label = "mouth")
    val tap = if (onClick == null) Modifier else Modifier.clickable(onClick = onClick)
    Canvas(modifier.size(size).then(tap)) {
        val w = this.size.width
        drawCircle(if (gender == Gender.F) Color(0xFFCFD8FF) else Color(0xFFFFE3A8), w / 2, Offset(w / 2, w / 2))
        val look = Look(blink, if (talking) mouth else 0f, mood)
        translate(top = bob * w * 0.012f) { if (gender == Gender.F) drawMissGrace(w, look) else drawMisterLeo(w, look) }
    }
}

/** Мгновенный вид лица: закрытость глаз (0..1), открытость рта (0..1), настроение. */
private class Look(val blink: Float, val open: Float, val mood: Mood)

private fun DrawScope.shoulders(w: Float, color: Color) {
    val body = Path().apply {
        moveTo(w * 0.12f, w); cubicTo(w * 0.14f, w * 0.72f, w * 0.34f, w * 0.68f, w * 0.5f, w * 0.68f)
        cubicTo(w * 0.66f, w * 0.68f, w * 0.86f, w * 0.72f, w * 0.88f, w); close()
    }
    drawPath(body, color)
}

/** Лицо: овал, глаза (моргают), румянец у радостного и брови домиком у огорчённого. */
private fun DrawScope.face(w: Float, look: Look) {
    drawCircle(SKIN, w * 0.25f, Offset(w * 0.5f, w * 0.44f))
    val height = maxOf(w * 0.008f, w * 0.05f * (1f - look.blink))
    for (x in listOf(0.42f, 0.58f)) drawOval(INK, Offset(w * (x - 0.024f), w * 0.44f - height / 2), Size(w * 0.048f, height))
    if (look.mood == Mood.HAPPY) for (x in listOf(0.35f, 0.65f)) drawCircle(Color(0x66FF6B81), w * 0.035f, Offset(w * x, w * 0.51f))
    if (look.mood == Mood.SAD) {
        drawLine(INK, Offset(w * 0.36f, w * 0.385f), Offset(w * 0.45f, w * 0.36f), w * 0.016f)
        drawLine(INK, Offset(w * 0.64f, w * 0.385f), Offset(w * 0.55f, w * 0.36f), w * 0.016f)
    }
}

/** Рот: пока говорит — открывается и закрывается; иначе улыбка, ровная линия или грустная дуга по настроению. */
private fun DrawScope.mouth(w: Float, y: Float, look: Look) {
    val line = Stroke(w * 0.016f)
    when {
        look.open > 0.05f -> drawOval(MOUTH, Offset(w * 0.465f, w * y), Size(w * 0.07f, w * (0.012f + 0.05f * look.open)))
        look.mood == Mood.HAPPY -> drawArc(INK, 10f, 160f, false, Offset(w * 0.43f, w * (y - 0.03f)), Size(w * 0.14f, w * 0.08f), style = line)
        look.mood == Mood.SAD -> drawArc(INK, 200f, 140f, false, Offset(w * 0.45f, w * (y + 0.005f)), Size(w * 0.1f, w * 0.06f), style = line)
        else -> drawArc(INK, 20f, 140f, false, Offset(w * 0.45f, w * (y - 0.02f)), Size(w * 0.1f, w * 0.06f), style = line)
    }
}

private fun DrawScope.drawMissGrace(w: Float, look: Look) {
    val hair = Color(0xFF4A2F27)
    drawCircle(hair, w * 0.1f, Offset(w * 0.5f, w * 0.14f))
    shoulders(w, Color(0xFF3B3F4F))
    val collar = Path().apply { moveTo(w * 0.4f, w * 0.69f); lineTo(w * 0.5f, w * 0.84f); lineTo(w * 0.6f, w * 0.69f); close() }
    drawPath(collar, Color.White)
    drawCircle(hair, w * 0.27f, Offset(w * 0.5f, w * 0.4f))
    face(w, look)
    val rim = Stroke(w * 0.017f)
    for (x in listOf(0.42f, 0.58f)) drawCircle(INK, w * 0.075f, Offset(w * x, w * 0.44f), style = rim)
    drawLine(INK, Offset(w * 0.495f, w * 0.44f), Offset(w * 0.505f, w * 0.44f), w * 0.017f)
    mouth(w, 0.55f, look)
    drawArc(hair, 180f, 180f, true, Offset(w * 0.25f, w * 0.2f), Size(w * 0.5f, w * 0.24f))
}

private fun DrawScope.drawMisterLeo(w: Float, look: Look) {
    val hair = Color(0xFF6B3E26)
    shoulders(w, Color(0xFFE0A526))
    face(w, look)
    drawArc(hair, 0f, 180f, true, Offset(w * 0.3f, w * 0.4f), Size(w * 0.4f, w * 0.3f))
    drawCircle(SKIN, w * 0.07f, Offset(w * 0.5f, w * 0.57f))
    mouth(w, 0.58f, look)
    drawArc(hair, 180f, 180f, true, Offset(w * 0.24f, w * 0.15f), Size(w * 0.52f, w * 0.32f))
    drawCircle(hair, w * 0.07f, Offset(w * 0.64f, w * 0.17f))
    val bow = Path().apply {
        moveTo(w * 0.5f, w * 0.74f); lineTo(w * 0.4f, w * 0.69f); lineTo(w * 0.4f, w * 0.79f); close()
        moveTo(w * 0.5f, w * 0.74f); lineTo(w * 0.6f, w * 0.69f); lineTo(w * 0.6f, w * 0.79f); close()
    }
    drawPath(bow, Color(0xFFD64545))
}

/** Карточка преподавателя: живой аватар (по нажатию здоровается), имя и характер. */
@Composable
fun TeacherHeader(gender: Int, name: String, info: String, onGreet: () -> Unit) {
    SettingCard {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            TeacherAvatar(gender, 72.dp, onClick = onGreet)
            Column(Modifier.weight(1f), Arrangement.spacedBy(2.dp)) {
                Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(info, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
