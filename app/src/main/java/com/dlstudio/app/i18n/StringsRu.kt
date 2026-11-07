package com.dlstudio.app.i18n

import com.dlstudio.app.study.MotivationKind
import com.dlstudio.app.settings.AccentColor
import com.dlstudio.app.settings.StorageStatus
import com.dlstudio.app.settings.ThemeMode

object StringsRu : Strings {
    override val tabStudy = "Учёба"; override val tabStats = "Статистика"; override val tabLibrary = "Библиотека"; override val tabSettings = "Настройки"; override val stubHint = "Раздел в разработке"
    override val settingTheme = "Тема"; override val settingLanguage = "Язык приложения"; override val settingAccent = "Акцентный цвет"; override val settingFolder = "Папка хранения"; override val settingAbout = "О нас"
    override val folderDefault = "Папка приложения"; override val aboutTitle = "DL Studio"; override val aboutText = "Карточки для изучения языков. Работает без интернета."; override val close = "Закрыть"
    override val viewCards = "Карточки"; override val viewList = "Список"; override val edit = "Изменить"; override val delete = "Удалить"; override val save = "Сохранить"; override val cancel = "Отмена"
    override val cardBack = "Обратная сторона"; override val cardHint = "Подсказка (необязательно)"; override val cardExplanation = "Пояснение (необязательно)"; override val cardExample = "Пример (необязательно)"
    override val showAnswer = "Показать перевод"; override val hintButton = "Подсказка"; override val speak = "Озвучить"; override val previous = "Назад"; override val next = "Дальше"; override val noCards = "Добавьте карточки в библиотеку — и здесь начнётся учёба."
    override val statsOverview = "Обзор обучения"; override val statsProjects = "Проекты"; override val statsDecks = "Колоды"; override val statsCards = "Карточки"; override val statsStudied = "Изучено"; override val statsProgress = "Прогресс библиотеки"; override val statsEmpty = "Здесь появится прогресс после первой просмотренной карточки."
    override val speechVoice = "Озвучка Android"; override val speechRate = "Скорость речи"; override val speechPitch = "Высота голоса"; override val dailyCards = "Карточек в сессии"; override val reminders = "Напоминания об учёбе"; override val reminderTime = "Время напоминания"
    override val allDecks = "Все колоды"; override val allCards = "Все карточки"; override val chooseProject = "Выберите проект"; override val chooseDeck = "Выберите колоду"; override val flipHint = "Нажмите на карточку, чтобы перевернуть"; override val voicePreview = "Пример произношения"; override val voicePackagesEmpty = "Системные языковые пакеты не найдены"; override val due = "к повтору"; override val overdue = "просрочено"; override val statsDue = "К повтору"; override val statsOverdue = "Просрочено"; override val statsToday = "Повторов сегодня"; override val statsAverage = "Среднее время ответа"; override val statsStreak = "Серия дней"; override val statsReviewsChart = "Повторы за 14 дней"; override val statsIntervals = "Распределение интервалов"
    override val importFormat = "Одна карточка в строке: слово и перевод через «;», табуляцию или « - », например «cat;кот». Подходят CSV, TSV, TXT (в том числе экспорт Anki и Quizlet) и JSON. Первая строка «слово;перевод;колода» разложит карточки по колодам."
    override val importPick = "Выбрать файл"; override val importEmpty = "В файле не нашлось карточек. Нужны строки вида «слово;перевод»."
    override val sectionLook = "Внешний вид"; override val sectionSpeech = "Озвучка"; override val sectionStudy = "Учёба"; override val sectionData = "Данные"
    override val yesterdayTitle = "Вчера"; override val yesterdayEmpty = "Вчера занятий не было"; override val streakTitle = "Серия"; override val forecastTitle = "Ближайшие 7 дней"
    override val projectsTitle = "Проекты"; override val doneWord = "сделано"; override val newRound = "Круг пройден. Начинаем новый, порядок будет другим…"; override val learn = "Учить"
    override fun reviewsCount(n: Int) = "$n ${ruPlural(n, "повтор", "повтора", "повторов")}"
    override fun motivation(kind: MotivationKind, n: Int) = when (kind) {
        MotivationKind.EMPTY -> "Добавьте первые карточки в библиотеке, и начнём."
        MotivationKind.START -> "Первая сессия самая важная. Хватит и пяти минут."
        MotivationKind.DONE_TODAY -> "Сегодня уже ${reviewsCount(n)}. План выполнен, так держать!"
        MotivationKind.STREAK -> "Серия: $n ${ruPlural(n, "день", "дня", "дней")}. Одна сессия сегодня сохранит её."
        MotivationKind.CATCH_UP -> "Просрочено: $n. Разберите их сегодня, и память скажет спасибо."
        MotivationKind.MATURE -> "Выучено $n ${ruPlural(n, "карточка", "карточки", "карточек")}. Каждый день делает вас сильнее."
        MotivationKind.KEEP -> "Маленькие шаги каждый день дают большой результат."
    }
    override val tapToContinue = "Листайте карточки свайпом"; override val tapToFlip = "Нажмите, чтобы увидеть перевод"
    override val startStudy = "Начать"; override val studyAll = "Вся библиотека"; override val nothingDue = "На сегодня всё!"; override val nothingDueHint = "Новые карточки и повторы появятся здесь, когда придёт срок."
    override val practiceMore = "Позаниматься сверх плана"; override val sessionDone = "Сессия завершена"; override val backToList = "К списку"
    override val checkAnswer = "Проверить"; override val listenPrompt = "Послушайте слово"; override val writePrompt = "Введите ответ"
    override val todayTitle = "Сегодня"
    override val freshWord = "новых"
    override val statsNew = "Новые"
    override val statsAccuracy = "Точность"
    override val statsMature = "Выучено"
    override val secondsShort = "с"
    override val daysShort = "д"
    override val libraryFailed = "Не удалось открыть базу. Данные не тронуты — правки отключены. Проверьте папку хранения в настройках."
    override val reminderChannel = "Учёба"
    override val reminderText = "Время повторить карточки"
    override val on = "Включены"
    override val off = "Выключены"
    override val notificationsNeeded = "Разрешите уведомления в настройках Android, иначе напоминания не придут."
    override val statsReviewsTitle = "Повторы за 14 дней"
    override val weekdays = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
    override fun reminderDue(n: Int) = "К повтору: $n ${ruPlural(n, "карточка", "карточки", "карточек")}"
    override fun sessionProgress(done: Int, total: Int) = "$done из $total"
    override fun sessionSummary(know: Int, hard: Int, again: Int) = "Знал: $know · Трудно: $hard · Не знал: $again"
    override fun addTitle(level: Int) = listOf("Новый проект", "Новая колода", "Новая карточка")[level]
    override fun editTitle(level: Int) = listOf("Изменить проект", "Изменить колоду", "Изменить карточку")[level]
    override fun nameLabel(level: Int) = listOf("Название проекта", "Название колоды", "Лицевая сторона")[level]
    override fun emptyText(level: Int) = listOf(
        "Проектов пока нет. Создайте первый, например «Английский B1».",
        "В проекте пока нет колод. Создайте первую.",
        "В колоде пока нет карточек. Добавьте первую."
    )[level]
    override fun contentCount(level: Int, n: Int) =
        if (level == 0) "$n ${ruPlural(n, "колода", "колоды", "колод")}" else "$n ${ruPlural(n, "карточка", "карточки", "карточек")}"
    override fun deleteQuestion(level: Int, name: String) =
        "Удалить ${listOf("проект", "колоду", "карточку")[level]} «$name»?" + if (level < 2) " Всё внутри тоже будет удалено." else ""
    override fun importTitle(level: Int) = listOf("Импорт проекта из файла", "Импорт колоды из файла", "Импорт карточек из файла")[level]
    override fun importOutcome(level: Int) = listOf(
        "Файл станет новым проектом: название — по имени файла, внутри будут колоды и карточки.",
        "Файл станет новой колодой в этом проекте.",
        "Карточки из файла добавятся в эту колоду."
    )[level]
    override fun importDone(n: Int) = "Добавлено: $n ${ruPlural(n, "карточка", "карточки", "карточек")}"
    override fun accentName(accent: AccentColor) = listOf("Фиолетовый", "Синий", "Бирюзовый", "Зелёный", "Янтарный", "Розовый", "Красный")[accent.ordinal]
    override fun themeName(mode: ThemeMode) = if (mode == ThemeMode.DARK) "Тёмная (AMOLED)" else "Светлая"
    override fun status(status: StorageStatus) = when (status) {
        StorageStatus.NONE -> ""
        StorageStatus.MOVING -> "Переношу файлы…"
        StorageStatus.MOVED -> "Готово: файлы перенесены, старая папка удалена"
        StorageStatus.ERROR_INSIDE -> "Нельзя выбрать папку внутри DL-Studio"
        StorageStatus.ERROR_IO -> "Не удалось перенести, данные остались на старом месте"
    }
}
