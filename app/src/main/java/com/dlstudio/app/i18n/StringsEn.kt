package com.dlstudio.app.i18n

import com.dlstudio.app.settings.AccentColor
import com.dlstudio.app.settings.AccentPalette
import com.dlstudio.app.settings.ThemeMode

object StringsEn : Strings {
    override val tabMap = "Map"; override val tabProgress = "Progress"; override val tabSettings = "Settings"

    override val continueLabel = "Continue"
    override val mapEmptyTitle = "No topics yet"; override val mapEmptyHint = "Download topics in settings or install a topic file."
    override val openSettings = "Open settings"; override val noGroup = "Topics"
    override fun groupProgress(done: Int, total: Int) = "$done of $total ${if (total == 1) "topic" else "topics"}"
    override fun cardsCount(n: Int) = "$n ${if (n == 1) "card" else "cards"}"

    override val stageTheory = "Theory"; override val stageCards = "Cards"; override val stagePractice = "Practice"
    override val previous = "Back"; override val next = "Next"; override val finish = "Done"; override val speak = "Speak"
    override val tapToFlip = "Tap the card to flip it"; override val know = "Know it"; override val again = "Again"
    override fun cardCounter(index: Int, total: Int, known: Int) = "$index of $total · known $known"
    override val correct = "Correct!"
    override fun wrongAnswer(answer: String) = "Correct answer: $answer"
    override fun quizResult(ok: Int, total: Int) = "$ok of $total correct"
    override val retry = "Try again"; override val stageDone = "Stage complete"; override val topicDone = "Topic complete!"; override val seeResult = "Result"

    override fun levelTitle(level: Int) = "Level $level"
    override fun xpToNext(xp: Int) = "To next level: $xp XP"
    override fun streakDays(n: Int) = "$n ${if (n == 1) "day" else "days"} in a row"
    override fun topicsDone(done: Int, total: Int) = "Topics completed: $done of $total"
    override val xpRulesTitle = "How to earn XP"
    override val xpRules = "Theory +10 · card marked “Know it” +2 · correct answer +5 · whole topic +50. Repeating finished work gives no XP."
    override val sealsTitle = "Level seals"

    override val sectionLook = "Appearance"; override val settingTheme = "Theme"; override val settingLanguage = "App language"; override val settingAccent = "Accent color"
    override fun themeName(mode: ThemeMode) = if (mode == ThemeMode.DARK) "Dark (AMOLED)" else "Light"
    override fun accentName(accent: AccentColor) = listOf("Violet", "Blue", "Green", "Amber", "Red", "Lavender", "Sky", "Mint", "Peach", "Crimson", "Plum", "Turquoise", "Lime", "Orange", "Magenta")[accent.ordinal]
    override fun accentPaletteName(palette: AccentPalette) = listOf("Classic", "Soft", "Bright")[palette.ordinal]

    override val sectionTopics = "Topics"; override val catalogAddress = "Catalog address"; override val catalogNotSet = "Not set"
    override val catalogHint = "An https folder that contains course.json. Topics download one by one, each checked against its checksum."
    override val refreshCatalog = "Refresh catalog"; override val fromFile = "From file"; override val downloadAll = "Download all"; override val download = "Download"
    override val availableTitle = "Available topics"; override val installedTitle = "Installed topics"; override val remove = "Remove"
    override val catalogLoading = "Loading the catalog…"; override val catalogFailed = "Couldn’t get the catalog. Check the address and your connection."
    override val downloadFailed = "Some topics didn’t download. Try again."; override val fileFailed = "That file doesn’t look like a topic: it needs a json or a zip with a topic."
    override val nothingNew = "All topics from the catalog are installed."
    override fun removeQuestion(title: String) = "Remove the topic “$title”? Your progress is kept."
    override val cancel = "Cancel"; override val save = "Save"

    override val aboutTitle = "DL Studio"; override val aboutText = "Courses by topic: theory, flashcards and practice. Works offline."
    override val aboutCopyright = "© 2026 DataLaw DesignLab. All rights reserved."
}
