package com.dlstudio.app.i18n

import com.dlstudio.app.settings.StorageStatus
import com.dlstudio.app.settings.ThemeMode

/** Все тексты интерфейса. Новый язык = новая реализация; забытая строка не соберётся. */
interface Strings {
    val tabStudy: String
    val tabStats: String
    val tabLibrary: String
    val tabSettings: String
    val stubHint: String
    val settingTheme: String
    val settingLanguage: String
    val settingFolder: String
    val settingAbout: String
    val folderDefault: String
    val aboutTitle: String
    val aboutText: String
    val close: String
    fun themeName(mode: ThemeMode): String
    fun status(status: StorageStatus): String
}
