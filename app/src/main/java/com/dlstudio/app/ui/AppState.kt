package com.dlstudio.app.ui

import com.dlstudio.app.settings.AppLang
import com.dlstudio.app.settings.StorageStatus
import com.dlstudio.app.settings.ThemeMode

/** Всё, что показывают настройки; `folderName` null — папка приложения по умолчанию. */
data class AppState(
    val theme: ThemeMode,
    val lang: AppLang,
    val folderName: String?,
    val status: StorageStatus = StorageStatus.NONE
)
