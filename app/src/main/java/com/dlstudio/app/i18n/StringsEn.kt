package com.dlstudio.app.i18n

import com.dlstudio.app.progress.DayPart
import com.dlstudio.app.progress.GoalPhase
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
    override val goalTitle = "Daily goal"; override val goalDone = "Daily goal reached!"
    override fun goalProgress(done: Int, goal: Int) = "$done of $goal XP"
    override val sectionLearning = "Learning"; override val settingGoal = "Daily goal"; override fun goalValue(xp: Int) = "$xp XP a day"
    override val weekTitle = "This week"; override fun weekTotal(xp: Int) = "$xp XP this week"
    override val weekdays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    override val ranks = listOf("Newcomer", "Learner", "Practitioner", "Connoisseur", "Master", "Expert", "Mentor", "Guru", "Legend", "Champion")
    override val badgesTitle = "Badges"; override fun badgesCount(have: Int, total: Int) = "$have of $total"
    override val badgeNames = listOf("First steps", "First topic", "Three in a row", "Card collector", "Hundred points", "Sharp eye", "Week of fire", "Five topics", "Half a thousand", "A month without gaps")
    override val badgeHints = listOf("Earn 10 XP", "Finish a whole topic", "Study 3 days in a row", "Flip 25 cards", "Earn 100 XP", "Answer 20 questions correctly", "Study 7 days in a row", "Finish 5 topics", "Earn 500 XP", "Study 30 days in a row")
    override fun nextBadge(name: String, have: Int, target: Int) = "Next badge — “$name”: $have of $target"
    override val statTopics = "Topics done"; override val statCards = "Cards"; override val statAnswers = "Correct answers"; override val quoteMore = "Tap to change"
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

    override fun goalNudge(phase: GoalPhase, left: Int) = when (phase) {
        GoalPhase.START -> "Start with a single card: that’s +2 XP right away"
        GoalPhase.EARLY -> "$left XP to go — about ${(left + 4) / 5} correct ${if ((left + 4) / 5 == 1) "answer" else "answers"}"
        GoalPhase.HALF -> "More than halfway there! $left XP left"
        GoalPhase.CLOSE -> "Almost there: just $left XP more!"
        GoalPhase.DONE -> "Everything on top is a bonus toward your next level"
    }
    override val cheers = listOf(
        "You’ve got this. I believe in you! 💪",
        "One topic — and you can rest easy.",
        "Every word is a brick. Let’s keep building!",
        "Repetition is the mother of learning. A few cards?",
        "The streak won’t extend itself 😉",
        "Five minutes today and you’re ahead of yesterday’s you.",
        "Great day to learn something new!",
        "Mistakes are fine. That’s how things stick."
    )
    override val questsTitle = "Quests"; override val questGoal = "Hit today’s goal"
    override fun questBadge(name: String) = "Badge “$name”"; override fun questLevel(level: Int) = "Reach level $level"
    override val pathTitle = "Your path"

    override val continueLabel = "Continue"
    override val collapseAll = "Collapse all"; override val expandAll = "Expand all"
    override fun lastSeen(title: String) = "You left off at “$title”"
    override val mapEmptyTitle = "No topics yet"; override val mapEmptyHint = "Download topics in settings or install a file: a topic json or a zip with topics."
    override val openSettings = "Open settings"; override val noGroup = "Topics"
    override fun groupProgress(done: Int, total: Int) = "$done of $total ${if (total == 1) "topic" else "topics"}"
    override fun cardsCount(n: Int) = "$n ${if (n == 1) "card" else "cards"}"

    override val stageTheory = "Theory"; override val stageCards = "Cards"; override val stagePractice = "Practice"
    override val previous = "Back"; override val next = "Next"; override val finish = "Done"; override val speak = "Speak"
    override val tapToFlip = "Tap the card to flip it"
    override val checkPronunciation = "Check pronunciation"; override val pronListening = "Speak now…"
    override val pronNoSpeech = "Didn’t catch that. Tap the microphone and try again."
    override val pronNoPermission = "Microphone access is needed. Allow it in your phone’s settings."
    override val pronFailed = "Speech recognition isn’t available. Check your connection and the Google speech service."
    override fun pronGreat(score: Int) = "Great! $score%"
    override fun pronGood(score: Int) = "Almost! $score% — try once more"
    override fun pronRetry(score: Int) = "Not quite yet: $score%. Listen and repeat"
    override fun pronHeard(heard: String) = "Heard: “$heard”"
    override val pronNoPack = "There’s no speech recognition pack for this language."
    override val tileVowel = "vowels"; override val tileConsonant = "consonants"
    override val listenPrompt = "Listen and pick what you heard"; override val speakPrompt = "Say it out loud"
    override val dictationPrompt = "Listen and type what you heard"; override val typeAnswer = "Your answer"
    override val check = "Check"; override val skip = "Skip"; override val speakSkipped = "No worries, let’s move on"
    override val bankPlaceholder = "Tap the words below to build the answer"
    override val matchPrompt = "Match the pairs"; override val matchHint = "Tap any item, then its match on the other side"; override val matchWrong = "Pairs matched, with a few mistakes"
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
    override val resetProgressHint = "Clears your streak and completed stages. XP, level and topics stay."
    override val resetQuestion = "Reset progress? Completed stages and your streak will be deleted and can’t be restored. XP and level are kept."; override val reset = "Reset"

    override val sectionTopics = "Topics"; override val fromFile = "Add from file"; override val download = "Download"
    override val installedTitle = "Installed topics"; override val remove = "Remove"; override val removeAll = "Remove all topics"
    override val fileFailed = "That file doesn’t look like a topic: it needs a topic json or a zip with topics."
    override fun removeQuestion(title: String) = "Remove the topic “$title”? Your progress is kept."
    override fun removeAllQuestion(count: Int) = "Remove all $count topics? Your progress is kept and you can install them again."
    override fun removeManyQuestion(count: Int) = "Remove $count topics? Progress is kept."
    override val mediaMissing = "File not downloaded"
    override val noCourse = "No course"
    override val cancel = "Cancel"; override val save = "Save"

    override val sectionVoice = "Languages & voices"
    override val voiceIntro = "Languages come from the installed topics. Speech and pronunciation checks use Android’s built-in services: download the voices and packs you need, pick voices and speech speed here."
    override val voiceRate = "Speech speed"; override val rateSlow = "Slow"; override val rateNormal = "Normal"; override val rateFast = "Fast"
    override val voiceAuto = "Auto"
    override val voiceHigh = "high quality"; override val voiceNetwork = "needs internet"; override val voiceNotInstalled = "not downloaded"
    override val voicesLoading = "Looking for voices…"; override val voicesMore = "Download more voices…"; override val voiceLanguage = "Language"
    override val packOn = "Downloaded"; override val packOff = "Not downloaded"
    override val recogTitle = "Speech recognition"
    override val recogHint = "Used for pronunciation checks. With a language pack it works offline."
    override val packInstalled = "Pack downloaded, works offline"; override val packAvailable = "Pack not downloaded, internet needed"
    override val packDownloading = "Downloading…"; override val packUnsupported = "Pack status can’t be detected: if it’s installed, pronunciation checks work"
    override val packUnknown = "Status unknown: if the pack is installed, pronunciation checks work"

    override val aboutTitle = "DL Studio"; override val aboutText = "Courses by topic: theory, flashcards and practice. Works offline."
    override val aboutCopyright = "© 2026 DataLaw DesignLab. All rights reserved."

    override val genderFemale = "female"; override val genderMale = "male"
    override val statusRecog = "Recognition"
    override val statusNoLangs = "Install a topic and its languages, voices and packs will appear here."
    override val downloadNeeded = "Download what’s missing"; override val allReady = "Everything needed is installed"
    override val offerTitle = "Language packs needed"; override val later = "Later"
    override fun offerText(languages: String) = "The installed topics need more voices or recognition packs: $languages. Download what’s missing?"
    override val teachersTitle = "Teachers"
    override val teacherFName = "Miss Grace"; override val teacherMName = "Mr. Leo"
    override val teacherFInfo = "A strict teacher: glasses, business suit, pointer. Speaks clearly and to the point. Voices female lines and explanations."
    override val teacherMInfo = "A young enthusiast: beard, sweater, bow tie. Explains with a joke. Voices male lines."
    override val genderAutoHint = "The best-quality voice is picked automatically"
    override val voiceGenderHint = "Only voices whose gender is explicitly known are listed (per Google’s naming). Voices without a known gender are not used."
    override val tabOverview = "Overview"; override val tabStats = "Statistics"; override val downloadPack = "Download recognition pack"
    override fun voiceCounts(installed: Int, known: Int) = "Voices downloaded: $installed, with a known gender: $known"
    override val statsEmpty = "Nothing to show yet: do exercises and flip cards or theory pages, and statistics will appear here."
    override val statsRight = "Correct"; override val statsWrong = "Wrong"; override val statsAccuracy = "Accuracy"
    override val statsCards = "Cards viewed"; override val statsPages = "Theory pages"; override val statsListens = "Times listened"
    override val statsAnswers = "Answers"; override val statsXpDays = "XP by day (14 days)"; override val statsAnswerDays = "Answers by day (14 days)"
    override val statsCardDays = "Cards by day (14 days)"; override val statsByKind = "By exercise type"; override val statsByTopic = "By topic"
    override val statsPron = "Pronunciation"; override val statsTries = "Attempts"; override val statsAvg = "Average score"; override val statsBest = "Best score"
    override val statsGreat = "Great"; override val statsGood = "Good"; override val statsRetry = "Try again"
    override val kindChoice = "Choice"; override val kindListen = "Listening"; override val kindSpeak = "Speaking"
    override val kindBank = "Word bank"; override val kindFill = "Fill in"; override val kindMatch = "Pairs"
}
