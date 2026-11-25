package com.dlstudio.app.i18n

import com.dlstudio.app.progress.DayPart
import com.dlstudio.app.progress.GoalPhase
import com.dlstudio.app.progress.Mood
import com.dlstudio.app.content.QuizKind
import com.dlstudio.app.settings.ThemeMode
import com.dlstudio.app.speech.How

/** Все тексты интерфейса. Новый язык = новая реализация; забытая строка не соберётся. */
interface Strings {
    val tabStart: String; val tabMap: String; val tabProgress: String; val tabSettings: String

    fun greeting(part: DayPart): String
    fun moodText(mood: Mood, streak: Int): String
    val goalTitle: String; val goalDone: String; fun goalProgress(done: Int, goal: Int): String
    val sectionLearning: String; val settingGoal: String; fun goalValue(xp: Int): String
    val weekTitle: String; fun weekTotal(xp: Int): String; val weekdays: List<String>
    val ranks: List<String>
    val badgesTitle: String; fun badgesCount(have: Int, total: Int): String; val badgeNames: List<String>; val badgeHints: List<String>
    fun nextBadge(name: String, have: Int, target: Int): String
    val statTopics: String; val statCards: String; val statAnswers: String; val quoteMore: String
    val quoteTitle: String; val quotes: List<String>; val tipTitle: String; val tips: List<String>

    fun goalNudge(phase: GoalPhase, left: Int): String
    val cheers: List<String>
    val questsTitle: String; val questGoal: String; fun questBadge(name: String): String; fun questLevel(level: Int): String
    val pathTitle: String

    val continueLabel: String
    val collapseAll: String; val expandAll: String; fun lastSeen(title: String): String
    val mapEmptyTitle: String; val mapEmptyHint: String; val openSettings: String; val noGroup: String
    fun groupProgress(done: Int, total: Int): String
    fun cardsCount(n: Int): String

    val stageTheory: String; val stageCards: String; val stagePractice: String
    val previous: String; val next: String; val finish: String; val speak: String
    val tapToFlip: String
    val checkPronunciation: String; val pronListening: String; val pronNoSpeech: String; val pronNoPermission: String; val pronFailed: String
    fun pronGreat(score: Int): String
    fun pronGood(score: Int): String
    fun pronRetry(score: Int): String
    fun pronHeard(heard: String): String
    val pronNoPack: String
    fun cardCounter(index: Int, total: Int, seen: Int): String
    val correct: String
    fun quizKind(kind: QuizKind, dictation: Boolean): String
    fun resultHeadline(good: Boolean): String
    fun wrongAnswer(answer: String): String
    fun quizResult(ok: Int, total: Int): String
    val tileVowel: String; val tileConsonant: String
    val listenPrompt: String; val speakPrompt: String; val dictationPrompt: String; val quietOn: String; val quietOff: String; val typeAnswer: String; val check: String; val skip: String; val speakSkipped: String
    val bankPlaceholder: String; val matchPrompt: String; val matchHint: String; val matchWrong: String
    val retry: String; val stageDone: String; val topicDone: String; val seeResult: String

    fun levelTitle(level: Int): String
    fun xpToNext(xp: Int): String
    fun streakDays(n: Int): String
    fun soundSkipped(n: Int): String
    fun soundLeft(n: Int): String
    fun onlyOpen(n: Int): String
    val again: String
    fun topicsDone(done: Int, total: Int): String
    val xpRulesTitle: String; val xpRules: String; val sealsTitle: String

    val sectionLook: String; val settingTheme: String; val settingLanguage: String; val settingAccent: String
    fun themeName(mode: ThemeMode): String

    val sectionData: String; val resetProgress: String; val resetProgressHint: String; val resetQuestion: String; val reset: String

    val sectionTopics: String; val fromFile: String; val download: String
    val installedTitle: String; val remove: String; val removeAll: String; val fileFailed: String
    fun removeQuestion(title: String): String
    fun removeAllQuestion(count: Int): String
    fun removeManyQuestion(count: Int): String
    val noCourse: String; val mediaMissing: String
    val cancel: String; val save: String

    val sectionVoice: String; val voiceIntro: String; val voiceRate: String; val rateSlow: String; val rateNormal: String; val rateFast: String
    val voiceAuto: String; val voiceHigh: String; val voiceNetwork: String; val voiceNotInstalled: String
    val voicesLoading: String; val voicesMore: String; val voiceLanguage: String; val packOn: String; val packOff: String
    val recogTitle: String; val recogHint: String
    val charactersTitle: String; val genderAutoHint: String; val voiceGenderHint: String; val genderFemale: String; val genderMale: String
    val statusFemale: String; val statusMale: String; val statusRecog: String; val statusNoLangs: String

    fun characterName(id: String): String
    fun characterRole(id: String): String
    fun characterBio(id: String): String
    fun voiceHow(how: How): String
    val sayHello: String; val introAgain: String; val introAgainHint: String; val portraitsCredit: String
    fun castTitle(course: String): String
    val castText: String; val castStart: String
    fun meetTitle(name: String): String
    val dialogueListen: String; val listenAll: String; val dialogueStop: String
    val problemNoEngine: String; val problemNoLanguage: String; val problemOk: String
    val downloadNeeded: String; val allReady: String; val offerTitle: String; val later: String
    fun offerText(languages: String): String
    val packInstalled: String; val packAvailable: String; val packDownloading: String; val packUnsupported: String; val packUnknown: String

    val aboutTitle: String; val aboutText: String; val aboutCopyright: String
    val tabOverview: String; val tabStats: String; val downloadPack: String; fun voiceCounts(installed: Int, known: Int): String
    val statsEmpty: String; val statsRight: String; val statsWrong: String; val statsAccuracy: String; val statsCards: String; val statsPages: String
    val statsListens: String; val statsAnswers: String; val statsXpDays: String; val statsAnswerDays: String; val statsCardDays: String
    val statsByKind: String; val statsByTopic: String; val statsPron: String; val statsTries: String; val statsAvg: String; val statsBest: String
    val statsGreat: String; val statsGood: String; val statsRetry: String
    val kindChoice: String; val kindListen: String; val kindSpeak: String; val kindBank: String; val kindFill: String; val kindMatch: String
}
