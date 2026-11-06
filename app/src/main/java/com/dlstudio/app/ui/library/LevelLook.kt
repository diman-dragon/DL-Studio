package com.dlstudio.app.ui.library

import androidx.compose.ui.graphics.Color
import com.dlstudio.app.R

/** Оформление уровней: 0 — проект, 1 — колода, 2 — карточка (градиент плитки и иконка). */
object LevelLook {
    val colors = listOf(
        listOf(Color(0xFFA77BFF), Color(0xFFFF4FD8)),
        listOf(Color(0xFFFFB02E), Color(0xFFFF3D77)),
        listOf(Color(0xFF2EE6FF), Color(0xFF4A5BFF))
    )
    val icons = listOf(R.drawable.ic_lv_project, R.drawable.ic_lv_deck, R.drawable.ic_lv_card)
}
