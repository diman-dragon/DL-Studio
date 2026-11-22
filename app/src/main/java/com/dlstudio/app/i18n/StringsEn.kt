package com.dlstudio.app.i18n

import com.dlstudio.app.progress.DayPart
import com.dlstudio.app.progress.Mood
import com.dlstudio.app.settings.ThemeMode

object StringsEn : Strings {
    override val tabStart = "Start"; override val tabMap = "Map"; override val tabProgress = "Progress"; override val tabSettings = "Settings"

    override fun greeting(part: DayPart) = when (part) {
        DayPart.MORNING -> "Good morning!"; DayPart.DAY -> "Good afternoon!"; DayPart.EVENING -> "Good evening!"; DayPart.NIGHT -> "Can’t sleep? Let’s learn"
    }
    override fun moodText(mood: Mood, streak: Int) = when (mood) {
        Mood.FIRST_STEP -> "The first step matters most. Open any topic — five minutes is enough."
        Mood.DONE_TODAY -> "You’ve already studied today. Streak: ${streakDays(streak)}. Keep it up!"
        Mood.KEEP_STREAK -> "Streak: ${streakDays(streak)}. One session today keeps it going."
        Mood.COMEBACK -> "Welcome back! Start with a single card — the rest will follow."
    }
    override val quoteTitle = "Thought of the day"
    override val quotes = listOf(
        "A small step every day beats a big push once a month.",
        "You don’t have to know everything. Just know a little more than yesterday.",
        "A mistake isn’t a failure — it’s a hint about where to look next.",
        "The road is walked by the one who keeps walking.",
        "Whoever has started is already ahead of those still thinking about it.",
        "Habit beats motivation: make studying a part of your day.",
        "Hard things get easy after the tenth repetition.",
        "Don’t compare yourself with others — compare with who you were yesterday.",
        "Little by little, a little becomes a lot.",
        "A good day begins with one thing learned.",
        "Progress isn’t always visible, but it always adds up.",
        "Speed doesn’t matter. What matters is that you don’t stop."
    )
    override val tipTitle = "Tip of the day"
    override val tips = listOf(
        "Ten minutes every day works better than two hours once a week.",
        "Try to recall the answer yourself before flipping the card — it sticks better.",
        "Say words out loud: the card voice helps with pronunciation.",
        "Review yesterday’s topic before starting a new one.",
        "Make up your own example for each card — a personal example is hard to forget.",
        "Study at the same time each day: the habit forms faster.",
        "Tired? Take a break. Your brain keeps memorising while you rest."
    )

    override val continueLabel = "Continue"
    override val mapEmptyTitle = "No topics yet"; override val mapEmptyHint = "Download topics in settings or install a topic file."
    override val openSettings = "Open settings"; override val noGroup = "Topics"
    override fun groupProgress(done: Int, total: Int) = "$done of $total ${if (total == 1) "topic" else "topics"}"
    override fun cardsCount(n: Int) = "$n ${if (n == 1) "card" else "cards"}"

    override val stageTheory = "Theory"; override val stageCards = "Cards"; override val stagePractice = "Practice"
    override val previous = "Back"; override val next = "Next"; override val finish = "Done"; override val speak = "Speak"
    override val tapToFlip = "Tap the card to flip it"
    override fun cardCounter(index: Int, total: Int, seen: Int) = "$index of $total · seen $seen"
    override val correct = "Correct!"
    override fun wrongAnswer(answer: String) = "Correct answer: $answer"
    override fun quizResult(ok: Int, total: Int) = "$ok of $total correct"
    override val retry = "Try again"; override val stageDone = "Stage complete"; override val topicDone = "Topic complete!"; override val seeResult = "Result"

    override fun levelTitle(level: Int) = "Level $level"
    override fun xpToNext(xp: Int) = "To next level: $xp XP"
    override fun streakDays(n: Int) = "$n ${if (n == 1) "day" else "days"} in a row"
    override fun topicsDone(done: Int, total: Int) = "Topics completed: $done of $total"
    override val xpRulesTitle = "How to earn XP"
    override val xpRules = "Theory +10 · card you flip +2 · correct answer +5 · whole topic +50. Repeating finished work gives no XP."
    override val sealsTitle = "Level seals"

    override val sectionLook = "Appearance"; override val settingTheme = "Theme"; override val settingLanguage = "App language"; override val settingAccent = "Accent color"
    override fun themeName(mode: ThemeMode) = if (mode == ThemeMode.DARK) "Dark (AMOLED)" else "Light"

    override val sectionData = "Data"; override val resetProgress = "Reset progress"
    override val resetProgressHint = "Clears XP, your streak and completed stages. Topics stay."
    override val resetQuestion = "Reset all progress? XP, streak and completed stages will be deleted and can’t be restored."; override val reset = "Reset"

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
