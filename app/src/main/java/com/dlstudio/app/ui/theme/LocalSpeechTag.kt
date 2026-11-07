package com.dlstudio.app.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf

/** Язык (BCP 47) выбранного голоса озвучки: по нему распознаётся и произношение; null — определить по слову. */
val LocalSpeechTag = staticCompositionLocalOf<String?> { null }
