package com.dlstudio.app.ui.theme

import androidx.compose.ui.graphics.Color
import com.dlstudio.app.study.Grade

/** Цвет оценки: красный — «Не знал», янтарный — «Трудно», зелёный — «Знал». */
fun Grade.tint(): Color = when (this) {
    Grade.AGAIN -> Color(0xFFFF5C6C)
    Grade.HARD -> Color(0xFFFFB02E)
    Grade.KNOW -> Color(0xFF3DC768)
}
