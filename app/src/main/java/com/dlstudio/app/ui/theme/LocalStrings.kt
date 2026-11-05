package com.dlstudio.app.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import com.dlstudio.app.i18n.Strings
import com.dlstudio.app.i18n.StringsRu

/** Тексты текущего языка для любого экрана. */
val LocalStrings = staticCompositionLocalOf<Strings> { StringsRu }
