package com.dlstudio.app.i18n

import com.dlstudio.app.settings.AccentColor
import com.dlstudio.app.settings.StorageStatus
import com.dlstudio.app.settings.ThemeMode

object StringsEn : Strings {
    override val tabStudy = "Study"; override val tabStats = "Stats"; override val tabLibrary = "Library"; override val tabSettings = "Settings"; override val stubHint = "Coming soon"
    override val settingTheme = "Theme"; override val settingLanguage = "App language"; override val settingAccent = "Accent color"; override val settingFolder = "Storage folder"; override val settingAbout = "About"
    override val folderDefault = "App folder"; override val aboutTitle = "DL Studio"; override val aboutText = "Flashcards for learning languages. Works offline."; override val close = "Close"
    override val viewCards = "Cards"; override val viewList = "List"; override val edit = "Edit"; override val delete = "Delete"; override val save = "Save"; override val cancel = "Cancel"
    override val cardBack = "Back side"; override val cardHint = "Hint (optional)"; override val cardExplanation = "Explanation (optional)"; override val cardExample = "Example (optional)"
    override val showAnswer = "Show translation"; override val hintButton = "Hint"; override val speak = "Speak"; override val previous = "Previous"; override val next = "Next"; override val noCards = "Add cards to your library to start studying here."
    override val statsOverview = "Study overview"; override val statsProjects = "Projects"; override val statsDecks = "Decks"; override val statsCards = "Cards"; override val statsStudied = "Studied"; override val statsProgress = "Library progress"; override val statsEmpty = "Your progress will appear here after the first card."
    override val speechVoice = "Android speech"; override val speechRate = "Speech rate"; override val speechPitch = "Voice pitch"
    override val importFormat = "One card per line: a word and its translation separated by “;”, a tab or “ - ”, for example “cat;кот”. CSV, TSV, TXT (including Anki and Quizlet exports) and JSON work. A first row “word;translation;deck” splits the cards into decks."
    override val importPick = "Choose file"; override val importEmpty = "No cards found in the file. Lines like “word;translation” are expected."
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
    override fun importTitle(level: Int) = listOf("Import project from file", "Import deck from file", "Import cards from file")[level]
    override fun importOutcome(level: Int) = listOf(
        "The file becomes a new project named after the file, with decks and cards inside.",
        "The file becomes a new deck in this project.",
        "The cards from the file are added to this deck."
    )[level]
    override fun importDone(n: Int) = "Added $n ${if (n == 1) "card" else "cards"}"
    override fun accentName(accent: AccentColor) = listOf("Violet", "Blue", "Teal", "Green", "Amber", "Pink", "Red")[accent.ordinal]
    override fun themeName(mode: ThemeMode) = if (mode == ThemeMode.DARK) "Dark (AMOLED)" else "Light"
    override fun status(status: StorageStatus) = when (status) {
        StorageStatus.NONE -> ""
        StorageStatus.MOVING -> "Moving files…"
        StorageStatus.MOVED -> "Done: files moved, old folder deleted"
        StorageStatus.ERROR_INSIDE -> "Pick a folder outside of DL-Studio"
        StorageStatus.ERROR_IO -> "Move failed, your data stayed in the old place"
    }
}
