package com.dlstudio.app.i18n

import com.dlstudio.app.settings.AccentColor
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
    val settingAccent: String
    val settingFolder: String
    val settingAbout: String
    val folderDefault: String
    val aboutTitle: String
    val aboutText: String
    val close: String
    val viewCards: String
    val viewList: String
    val edit: String
    val delete: String
    val save: String
    val cancel: String
    val cardBack: String
    val importFormat: String
    val importPick: String
    val importEmpty: String
    /** Тексты библиотеки по уровню: 0 — проект, 1 — колода, 2 — карточка. */
    fun addTitle(level: Int): String
    fun editTitle(level: Int): String
    fun nameLabel(level: Int): String
    fun emptyText(level: Int): String
    fun contentCount(level: Int, n: Int): String
    fun deleteQuestion(level: Int, name: String): String
    fun importTitle(level: Int): String
    fun importOutcome(level: Int): String
    fun importDone(n: Int): String
    fun accentName(accent: AccentColor): String
    fun themeName(mode: ThemeMode): String
    fun status(status: StorageStatus): String
}
