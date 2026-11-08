package com.dlstudio.app.i18n

import com.dlstudio.app.study.CardMode
import com.dlstudio.app.study.MotivationKind
import com.dlstudio.app.study.PraiseKind
import com.dlstudio.app.study.SpeechSpeed
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
    override val speechVoice = "Android speech"; override val dailyCards = "Cards per session"; override val reminders = "Study reminders"; override val reminderTime = "Reminder time"
    override val allDecks = "All decks"; override val allCards = "All cards"; override val chooseProject = "Choose a project"; override val chooseDeck = "Choose a deck"; override val flipHint = "Tap the card to flip it"; override val voicePreview = "Voice preview"; override val voicePackagesEmpty = "No system language packs found"; override val due = "due"; override val overdue = "overdue"; override val statsDue = "Due"; override val statsOverdue = "Overdue"; override val statsToday = "Reviews today"; override val statsAverage = "Average answer time"; override val statsStreak = "Day streak"; override val statsReviewsChart = "Reviews over 14 days"; override val statsIntervals = "Interval distribution"
    override val importFormat = "One card per line: a word and its translation separated by “;”, a tab or “ - ”, for example “cat;кот”. CSV, TSV, TXT (including Anki and Quizlet exports) and JSON work. A first row “word;translation;deck” splits the cards into decks."
    override val importPick = "Choose file"; override val importEmpty = "No cards found in the file. Lines like “word;translation” are expected."
    override val sectionLook = "Appearance"; override val sectionSpeech = "Speech"; override val sectionStudy = "Study"; override val sectionData = "Data"
    override val yesterdayTitle = "Yesterday"; override val yesterdayEmpty = "No study yesterday"; override val streakTitle = "Streak"; override val forecastTitle = "Next 7 days"
    override val projectsTitle = "Projects"; override val doneWord = "Done"; override val newRound = "Round complete. Starting a new one in a different order…"; override val learn = "Study"
    override val sectionModes = "Card modes"; override val sectionHelpers = "Helpers"; override val modeLast = "At least one mode is required"
    override val pronounceSetting = "Pronunciation check (microphone)"; override val celebrateSetting = "Animations and cheers"
    override val statsBestStreak = "Best streak"; override val statsWeek = "Last 7 days"; override val statsWeekTrend = "vs previous week"
    override val writeRight = "Correct!"; override fun writeWrong(correct: String) = "Not quite: $correct"
    override fun modeName(mode: CardMode) = when (mode) {
        CardMode.FLIP_FWD -> "Word with flip"
        CardMode.FLIP_REV -> "Translation with flip"
        CardMode.CHOICE_FWD -> "Choose the translation"
        CardMode.CHOICE_REV -> "Choose the word"
        CardMode.LISTEN -> "Listening: word by ear"
        CardMode.WRITE_FWD -> "Type the translation"
        CardMode.WRITE_REV -> "Type the word"
        CardMode.PRONOUNCE -> "Pronunciation: say the word"
        CardMode.REPEAT -> "Repeat after the speaker"
    }
    override fun speakLabel(speed: SpeechSpeed) = when (speed) { SpeechSpeed.NORMAL -> "Speak"; SpeechSpeed.SLOW -> "Slowly"; SpeechSpeed.SLOWER -> "Even slower" }
    override val sectionFrequency = "Frequency"; override val flipShare = "Flip cards"
    override val flipShareHint = "Share of word and translation cards with a flip; the rest is spread across the modes below."
    override val pronouncePrompt = "Say it in the language you study"; override val repeatPrompt = "Repeat after the speaker"; override val revealAnswer = "Show answer"
    override val customSize = "Custom"; override val customSizeTitle = "Custom number of cards"; override val customSizeHint = "From 1 to 500"
    override fun pronounceError(code: Int) = "Speech recognition failed to start (code $code). Please try again"
    override fun praise(kind: PraiseKind, n: Int) = when (kind) {
        PraiseKind.RECORD_DAY -> "Best day in two weeks: $n reviews. Keep it up!"
        PraiseKind.UP_TODAY -> "$n more reviews than yesterday. Keep it up!"
        PraiseKind.BEST_STREAK -> "New record! $n ${if (n == 1) "day" else "days"} in a row. Don't stop!"
        PraiseKind.WEEK_UP -> "$n% more reviews than last week. Great pace!"
        PraiseKind.ACCURACY -> "Accuracy $n%: you remember really well. Keep it up!"
        PraiseKind.KEEP -> "Every review is a step forward. Keep it up!"
    }
    override fun sessionPraise(level: Int) = listOf("What matters is that you practised. Repetition will help!", "Good work, better each time!", "Excellent result! Keep it up!")[level]
    override fun reviewsCount(n: Int) = "$n ${if (n == 1) "review" else "reviews"}"
    override fun motivation(kind: MotivationKind, n: Int) = when (kind) {
        MotivationKind.EMPTY -> "Add your first cards in the library and we will begin."
        MotivationKind.START -> "The first session matters most. Five minutes is enough."
        MotivationKind.DONE_TODAY -> "${reviewsCount(n)} done today. Plan complete, keep it up!"
        MotivationKind.STREAK -> "Streak: $n ${if (n == 1) "day" else "days"}. One session today keeps it alive."
        MotivationKind.CATCH_UP -> "$n overdue. Clear them today and your memory will thank you."
        MotivationKind.MATURE -> "$n ${if (n == 1) "card" else "cards"} learned. Every day makes you stronger."
        MotivationKind.KEEP -> "Small steps every day add up to big results."
    }
    override val tapToContinue = "Swipe to move between cards"; override val tapToFlip = "Tap to see the translation"
    override val startStudy = "Start"; override val studyAll = "Whole library"; override val nothingDue = "All done for today!"; override val nothingDueHint = "New cards and reviews will appear here when they are due."
    override val practiceMore = "Practice anyway"; override val sessionDone = "Session complete"; override val backToList = "Back to list"
    override val checkAnswer = "Check"; override val listenPrompt = "Listen to the word"; override val writePrompt = "Type the answer"
    override val todayTitle = "Today"
    override val freshWord = "new"
    override val statsNew = "New"
    override val statsAccuracy = "Accuracy"
    override val statsMature = "Mature"
    override val secondsShort = "s"
    override val daysShort = "d"
    override val libraryFailed = "Could not open the database. Your data is untouched and editing is disabled. Check the storage folder in settings."
    override val reminderChannel = "Study"
    override val reminderText = "Time to review your cards"
    override val on = "On"
    override val off = "Off"
    override val pronounceListening = "Listening… say it out loud"; override val pronounceOk = "Great, that sounded right"; override val pronounceClose = "Almost there, try once more"; override val pronounceMiss = "Not quite, listen and repeat"
    override val pronounceNoSpeech = "Could not hear you. Tap the microphone and speak louder"; override val pronounceUnavailable = "No speech recognition service found. Turn on Google voice input in Android settings"; override val pronounceDenied = "Allow the microphone in Android settings"
    override fun pronounceHeard(text: String) = "Heard: “$text”"
    override val notificationsNeeded = "Allow notifications in Android settings, or reminders will not arrive."
    override val statsReviewsTitle = "Reviews, 14 days"
    override val weekdays = listOf("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su")
    override fun reminderDue(n: Int) = "$n ${if (n == 1) "card" else "cards"} to review"
    override fun sessionProgress(done: Int, total: Int) = "$done of $total"
    override fun sessionSummary(know: Int, hard: Int, again: Int) = "Know: $know · Hard: $hard · Again: $again"
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
