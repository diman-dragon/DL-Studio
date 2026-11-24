package com.dlstudio.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dlstudio.app.speech.Gender

private val SKIN = Color(0xFFF2C9A5)
private val INK = Color(0xFF2B2B33)

/**
 * Преподаватель, нарисованный кодом. Gender.F — мисс Грейс: строгая учительница, пучок, очки, тёмный деловой костюм с белым воротничком.
 * Gender.M — мистер Лео: молодой энтузиаст, взъерошенная чёлка, борода, горчичный свитер и красная бабочка.
 */
@Composable
fun TeacherAvatar(gender: Int, size: Dp, modifier: Modifier = Modifier) {
    Canvas(modifier.size(size)) {
        val w = this.size.width
        drawCircle(if (gender == Gender.F) Color(0xFFCFD8FF) else Color(0xFFFFE3A8), w / 2, Offset(w / 2, w / 2))
        if (gender == Gender.F) drawMissGrace(w) else drawMisterLeo(w)
    }
}

private fun DrawScope.shoulders(w: Float, color: Color) {
    val body = Path().apply {
        moveTo(w * 0.12f, w); cubicTo(w * 0.14f, w * 0.72f, w * 0.34f, w * 0.68f, w * 0.5f, w * 0.68f)
        cubicTo(w * 0.66f, w * 0.68f, w * 0.86f, w * 0.72f, w * 0.88f, w); close()
    }
    drawPath(body, color)
}

private fun DrawScope.face(w: Float) {
    drawCircle(SKIN, w * 0.25f, Offset(w * 0.5f, w * 0.44f))
    drawCircle(INK, w * 0.022f, Offset(w * 0.42f, w * 0.44f))
    drawCircle(INK, w * 0.022f, Offset(w * 0.58f, w * 0.44f))
}

private fun DrawScope.drawMissGrace(w: Float) {
    val hair = Color(0xFF4A2F27)
    drawCircle(hair, w * 0.1f, Offset(w * 0.5f, w * 0.14f))
    shoulders(w, Color(0xFF3B3F4F))
    val collar = Path().apply { moveTo(w * 0.4f, w * 0.69f); lineTo(w * 0.5f, w * 0.84f); lineTo(w * 0.6f, w * 0.69f); close() }
    drawPath(collar, Color.White)
    drawCircle(hair, w * 0.27f, Offset(w * 0.5f, w * 0.4f))
    face(w)
    val rim = Stroke(w * 0.017f)
    drawCircle(INK, w * 0.075f, Offset(w * 0.42f, w * 0.44f), style = rim)
    drawCircle(INK, w * 0.075f, Offset(w * 0.58f, w * 0.44f), style = rim)
    drawLine(INK, Offset(w * 0.495f, w * 0.44f), Offset(w * 0.505f, w * 0.44f), w * 0.017f)
    drawArc(INK, 20f, 140f, false, Offset(w * 0.45f, w * 0.52f), Size(w * 0.1f, w * 0.06f), style = rim)
    drawArc(hair, 180f, 180f, true, Offset(w * 0.25f, w * 0.2f), Size(w * 0.5f, w * 0.24f))
}

private fun DrawScope.drawMisterLeo(w: Float) {
    val hair = Color(0xFF6B3E26)
    shoulders(w, Color(0xFFE0A526))
    face(w)
    drawArc(hair, 0f, 180f, true, Offset(w * 0.3f, w * 0.4f), Size(w * 0.4f, w * 0.3f))
    drawCircle(SKIN, w * 0.07f, Offset(w * 0.5f, w * 0.56f))
    drawArc(INK, 20f, 140f, false, Offset(w * 0.45f, w * 0.52f), Size(w * 0.1f, w * 0.05f), style = Stroke(w * 0.015f))
    drawArc(hair, 180f, 180f, true, Offset(w * 0.24f, w * 0.15f), Size(w * 0.52f, w * 0.32f))
    drawCircle(hair, w * 0.07f, Offset(w * 0.64f, w * 0.17f))
    val bow = Path().apply {
        moveTo(w * 0.5f, w * 0.74f); lineTo(w * 0.4f, w * 0.69f); lineTo(w * 0.4f, w * 0.79f); close()
        moveTo(w * 0.5f, w * 0.74f); lineTo(w * 0.6f, w * 0.69f); lineTo(w * 0.6f, w * 0.79f); close()
    }
    drawPath(bow, Color(0xFFD64545))
}

/** Карточка преподавателя: аватар, имя и характер. */
@Composable
fun TeacherHeader(gender: Int, name: String, info: String) {
    SettingCard {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            TeacherAvatar(gender, 72.dp)
            Column(Modifier.weight(1f), Arrangement.spacedBy(2.dp)) {
                Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(info, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
