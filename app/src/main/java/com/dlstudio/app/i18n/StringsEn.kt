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
    override val viewCards = "Cards"
    override val viewList = "List"
    override val edit = "Edit"
    override val delete = "Delete"
    override val save = "Save"
    override val cancel = "Cancel"
    override val cardBack = "Back side"
    override fun addTitle(level: Int) = listOf("New project", "New deck", "New card")[level]
    override fun editTitle(level: Int) = listOf("Edit project", "Edit deck", "Edit card")[level]
    override fun nameLabel(level: Int) = listOf("Project name", "Deck name", "Front side")[level]
    override fun emptyText(level: Int) = listOf(
        "No projects yet. Create the first one, for example “English B1”.",
        "This project has no decks yet. Create the first one.",
        "This deck has no cards yet. Add the first one."
    )[level]
    override fun contentCount(level: Int, n: Int) =
        if (level == 0) "$n ${if (n == 1) "deck" else "decks"}" else "$n ${if (n == 1) "card" else "cards"}"
    override fun deleteQuestion(level: Int, name: String) =
        "Delete the ${listOf("project", "deck", "card")[level]} “$name”?" + if (level < 2) " Everything inside will be deleted too." else ""
    override fun themeName(mode: ThemeMode) = if (mode == ThemeMode.DARK) "Dark (AMOLED)" else "Light"
    override fun status(status: StorageStatus) = when (status) {
        StorageStatus.NONE -> ""
        StorageStatus.MOVING -> "Moving files…"
        StorageStatus.MOVED -> "Done: files moved, old folder deleted"
        StorageStatus.ERROR_INSIDE -> "Pick a folder outside of DL-Studio"
        StorageStatus.ERROR_IO -> "Move failed, your data stayed in the old place"
    }
}
