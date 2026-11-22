package com.dlstudio.app.i18n

import com.dlstudio.app.progress.DayPart
import com.dlstudio.app.progress.Mood
import com.dlstudio.app.settings.ThemeMode

/** Все тексты интерфейса. Новый язык = новая реализация; забытая строка не соберётся. */
interface Strings {
    val tabStart: String; val tabMap: String; val tabProgress: String; val tabSettings: String

    fun greeting(part: DayPart): String
    fun moodText(mood: Mood, streak: Int): String
    val quoteTitle: String; val quotes: List<String>; val tipTitle: String; val tips: List<String>

    val continueLabel: String
    val mapEmptyTitle: String; val mapEmptyHint: String; val openSettings: String; val noGroup: String
    fun groupProgress(done: Int, total: Int): String
    fun cardsCount(n: Int): String

    val stageTheory: String; val stageCards: String; val stagePractice: String
    val previous: String; val next: String; val finish: String; val speak: String
    val tapToFlip: String
    fun cardCounter(index: Int, total: Int, seen: Int): String
    val correct: String
    fun wrongAnswer(answer: String): String
    fun quizResult(ok: Int, total: Int): String
    val retry: String; val stageDone: String; val topicDone: String; val seeResult: String

    fun levelTitle(level: Int): String
    fun xpToNext(xp: Int): String
    fun streakDays(n: Int): String
    fun topicsDone(done: Int, total: Int): String
    val xpRulesTitle: String; val xpRules: String; val sealsTitle: String

    val sectionLook: String; val settingTheme: String; val settingLanguage: String; val settingAccent: String
    fun themeName(mode: ThemeMode): String

    val sectionData: String; val resetProgress: String; val resetProgressHint: String; val resetQuestion: String; val reset: String

    val sectionTopics: String; val catalogAddress: String; val catalogNotSet: String; val catalogHint: String
    val refreshCatalog: String; val fromFile: String; val downloadAll: String; val download: String
    val availableTitle: String; val installedTitle: String; val remove: String
    val catalogLoading: String; val catalogFailed: String; val downloadFailed: String; val fileFailed: String; val nothingNew: String
    fun removeQuestion(title: String): String
    val cancel: String; val save: String

    val aboutTitle: String; val aboutText: String; val aboutCopyright: String
}
