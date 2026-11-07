package com.dlstudio.app.ui

import com.dlstudio.app.settings.AccentColor
import com.dlstudio.app.settings.AppLang
import com.dlstudio.app.settings.StorageStatus
import com.dlstudio.app.settings.ThemeMode
import com.dlstudio.app.study.StudyOptions

/** Всё, что показывают настройки; `folderName` null — папка приложения по умолчанию. */
data class AppState(
    val theme: ThemeMode,
    val lang: AppLang,
    val accent: AccentColor,
    val folderName: String?,
    val speechRate: Float = 1f,
    val speechPitch: Float = 1f,
    val speechVoiceName: String? = null,
    val status: StorageStatus = StorageStatus.NONE,
    val sessionSize: Int = 20,
    val remindersEnabled: Boolean = false,
    val reminderHour: Int = 19,
    val study: StudyOptions = StudyOptions()
)
