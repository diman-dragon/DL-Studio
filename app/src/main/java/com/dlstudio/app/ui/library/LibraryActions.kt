package com.dlstudio.app.ui.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dlstudio.app.ui.theme.LocalStrings

/** Плавающие кнопки библиотеки: сверху «импорт из файла», под ней «+» (подписи зависят от уровня). */
@Composable
fun LibraryActions(level: Int, onAdd: () -> Unit, onImport: () -> Unit, modifier: Modifier = Modifier) {
    val s = LocalStrings.current
    Column(modifier, Arrangement.spacedBy(14.dp), Alignment.CenterHorizontally) {
        ImportButton(s.importTitle(level), onImport)
        AddButton(s.addTitle(level), onAdd)
    }
}
