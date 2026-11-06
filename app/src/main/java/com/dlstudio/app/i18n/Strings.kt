package com.dlstudio.app.i18n

import com.dlstudio.app.settings.AccentColor
import com.dlstudio.app.settings.StorageStatus
import com.dlstudio.app.settings.ThemeMode

/** Все тексты интерфейса. Новый язык = новая реализация; забытая строка не соберётся. */
interface Strings {
    val tabStudy: String; val tabStats: String; val tabLibrary: String; val tabSettings: String; val stubHint: String
    val settingTheme: String; val settingLanguage: String; val settingAccent: String; val settingFolder: String; val settingAbout: String
    val folderDefault: String; val aboutTitle: String; val aboutText: String; val close: String; val viewCards: String; val viewList: String
    val edit: String; val delete: String; val save: String; val cancel: String; val cardBack: String; val cardHint: String
    val cardExplanation: String; val cardExample: String; val showAnswer: String; val hintButton: String; val speak: String
    val previous: String; val next: String; val noCards: String; val statsOverview: String; val statsProjects: String; val statsDecks: String
    val statsCards: String; val statsStudied: String; val statsProgress: String; val statsEmpty: String; val speechVoice: String
    val speechRate: String; val speechPitch: String; val dailyCards: String; val reminders: String; val reminderTime: String; val importFormat: String; val importPick: String; val importEmpty: String
    val allDecks: String; val allCards: String; val chooseProject: String; val chooseDeck: String; val flipHint: String; val voicePreview: String; val voicePackagesEmpty: String; val due: String; val overdue: String; val statsDue: String; val statsOverdue: String; val statsToday: String; val statsAverage: String; val statsStreak: String; val statsReviewsChart: String; val statsIntervals: String
    val sectionLook: String
    val sectionSpeech: String
    val sectionStudy: String
    val sectionData: String
    val gradeAgain: String
    val gradeHard: String
    val gradeKnow: String
    val tapToFlip: String
    val startStudy: String
    val studyAll: String
    val nothingDue: String
    val nothingDueHint: String
    val practiceMore: String
    val sessionDone: String
    val backToList: String
    val todayTitle: String
    val freshWord: String
    val statsNew: String
    val statsAccuracy: String
    val statsMature: String
    val secondsShort: String
    val daysShort: String
    val libraryFailed: String
    val reminderChannel: String
    val reminderText: String
    val on: String
    val off: String
    val notificationsNeeded: String
    val statsReviewsTitle: String
    val weekdays: List<String>
    fun reminderDue(n: Int): String
    fun interval(days: Int): String
    fun sessionProgress(done: Int, total: Int): String
    fun sessionSummary(know: Int, hard: Int, again: Int): String
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
