package com.dlstudio.app.ui

import com.dlstudio.app.settings.AccentColor
import com.dlstudio.app.settings.AppLang
import com.dlstudio.app.settings.FontSettings
import com.dlstudio.app.settings.StorageStatus
import com.dlstudio.app.settings.ThemeMode

/** Всё, что показывают настройки; `folderName` null — папка приложения по умолчанию. */
data class AppState(
    val theme: ThemeMode,
    val lang: AppLang,
    val accent: AccentColor,
    val folderName: String?,
    val speechVoiceName: String? = null,
    val speechVoiceLabel: String? = null,
    val status: StorageStatus = StorageStatus.NONE,
    val fonts: FontSettings = FontSettings()
)
