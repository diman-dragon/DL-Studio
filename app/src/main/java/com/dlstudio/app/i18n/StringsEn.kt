package com.dlstudio.app.i18n

import com.dlstudio.app.settings.StorageStatus
import com.dlstudio.app.settings.ThemeMode

object StringsEn : Strings {
    override val tabStudy = "Study"
    override val tabStats = "Stats"
    override val tabLibrary = "Library"
    override val tabSettings = "Settings"
    override val stubHint = "Coming soon"
    override val settingTheme = "Theme"
    override val settingLanguage = "App language"
    override val settingFolder = "Storage folder"
    override val settingAbout = "About"
    override val folderDefault = "App folder"
    override val aboutTitle = "DL Studio"
    override val aboutText = "Flashcards for learning languages. Works offline."
    override val close = "Close"
    override fun themeName(mode: ThemeMode) = if (mode == ThemeMode.DARK) "Dark (AMOLED)" else "Light"
    override fun status(status: StorageStatus) = when (status) {
        StorageStatus.NONE -> ""
        StorageStatus.MOVING -> "Moving files…"
        StorageStatus.MOVED -> "Done: files moved, old folder deleted"
        StorageStatus.ERROR_INSIDE -> "Pick a folder outside of DL-Studio"
        StorageStatus.ERROR_IO -> "Move failed, your data stayed in the old place"
    }
}
