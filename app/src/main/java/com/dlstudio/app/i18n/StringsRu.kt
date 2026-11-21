package com.dlstudio.app.i18n

import com.dlstudio.app.settings.AccentColor
import com.dlstudio.app.settings.AccentPalette
import com.dlstudio.app.settings.ThemeMode

object StringsRu : Strings {
    override val tabMap = "Карта"; override val tabProgress = "Прогресс"; override val tabSettings = "Настройки"

    override val mapEmptyTitle = "Пока нет тем"; override val mapEmptyHint = "Скачайте темы в настройках или поставьте файл темы."
    override val openSettings = "Открыть настройки"; override val noGroup = "Темы"
    override fun groupProgress(done: Int, total: Int) = "$done из $total ${ruPlural(total, "темы", "тем", "тем")}"
    override fun cardsCount(n: Int) = "$n ${ruPlural(n, "карточка", "карточки", "карточек")}"

    override val stageTheory = "Теория"; override val stageCards = "Карточки"; override val stagePractice = "Практика"
    override val previous = "Назад"; override val next = "Дальше"; override val finish = "Готово"; override val speak = "Озвучить"
    override val tapToFlip = "Нажмите на карточку, чтобы перевернуть"; override val know = "Знаю"; override val again = "Ещё раз"
    override fun cardCounter(index: Int, total: Int, known: Int) = "$index из $total · знаю $known"
    override val correct = "Верно!"
    override fun wrongAnswer(answer: String) = "Правильно: $answer"
    override fun quizResult(ok: Int, total: Int) = "Верно $ok из $total"
    override val retry = "Пройти снова"; override val stageDone = "Этап пройден"; override val topicDone = "Тема пройдена!"; override val seeResult = "Результат"

    override fun levelTitle(level: Int) = "Уровень $level"
    override fun xpToNext(xp: Int) = "До следующего уровня: $xp XP"
    override fun streakDays(n: Int) = "$n ${ruPlural(n, "день", "дня", "дней")} подряд"
    override fun topicsDone(done: Int, total: Int) = "Пройдено тем: $done из $total"
    override val xpRulesTitle = "Как получить XP"
    override val xpRules = "Теория +10 · карточка «Знаю» +2 · верный ответ +5 · тема целиком +50. За повтор уже пройденного XP не даётся."
    override val sealsTitle = "Печати уровней"

    override val sectionLook = "Внешний вид"; override val settingTheme = "Тема"; override val settingLanguage = "Язык приложения"; override val settingAccent = "Акцентный цвет"
    override fun themeName(mode: ThemeMode) = if (mode == ThemeMode.DARK) "Тёмная (AMOLED)" else "Светлая"
    override fun accentName(accent: AccentColor) = listOf("Фиолетовый", "Синий", "Зелёный", "Янтарный", "Красный", "Лавандовый", "Небесный", "Мятный", "Персиковый", "Малиновый", "Сливовый", "Бирюзовый", "Лаймовый", "Оранжевый", "Маджента")[accent.ordinal]
    override fun accentPaletteName(palette: AccentPalette) = listOf("Классика", "Мягкие", "Яркие")[palette.ordinal]

    override val sectionTopics = "Темы"; override val catalogAddress = "Адрес каталога"; override val catalogNotSet = "Не задан"
    override val catalogHint = "Адрес https-папки, где лежит course.json. Темы скачиваются по одной, каждая проверяется по контрольной сумме."
    override val refreshCatalog = "Обновить каталог"; override val fromFile = "Из файла"; override val downloadAll = "Скачать все"; override val download = "Скачать"
    override val availableTitle = "Доступные темы"; override val installedTitle = "Установленные темы"; override val remove = "Удалить"
    override val catalogLoading = "Загружаем каталог…"; override val catalogFailed = "Не удалось получить каталог. Проверьте адрес и сеть."
    override val downloadFailed = "Не все темы скачались. Попробуйте ещё раз."; override val fileFailed = "Файл не похож на тему: нужен json или zip с темой."
    override val nothingNew = "Все темы из каталога уже установлены."
    override fun removeQuestion(title: String) = "Удалить тему «$title»? Прогресс сохранится."
    override val cancel = "Отмена"; override val save = "Сохранить"

    override val aboutTitle = "DL Studio"; override val aboutText = "Курсы по темам: теория, карточки и практика. Работает без интернета."
    override val aboutCopyright = "© 2026 DataLaw DesignLab. Все права защищены."
}
