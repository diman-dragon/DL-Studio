package com.dlstudio.app.i18n

import com.dlstudio.app.progress.DayPart
import com.dlstudio.app.progress.Mood
import com.dlstudio.app.settings.ThemeMode

object StringsRu : Strings {
    override val tabStart = "Старт"; override val tabMap = "Карта"; override val tabProgress = "Прогресс"; override val tabSettings = "Настройки"

    override fun greeting(part: DayPart) = when (part) {
        DayPart.MORNING -> "Доброе утро!"; DayPart.DAY -> "Добрый день!"; DayPart.EVENING -> "Добрый вечер!"; DayPart.NIGHT -> "Не спится? Давайте учиться"
    }
    override fun moodText(mood: Mood, streak: Int) = when (mood) {
        Mood.FIRST_STEP -> "Первый шаг самый важный. Откройте любую тему — хватит и пяти минут."
        Mood.DONE_TODAY -> "Сегодня вы уже занимались. Серия: ${streakDays(streak)}. Так держать!"
        Mood.KEEP_STREAK -> "Серия: ${streakDays(streak)}. Одно занятие сегодня — и она продолжится."
        Mood.COMEBACK -> "С возвращением! Начните с одной карточки — остальное пойдёт само."
    }
    override val goalTitle = "Цель дня"; override val goalDone = "Цель дня выполнена!"
    override fun goalProgress(done: Int, goal: Int) = "$done из $goal XP"
    override val sectionLearning = "Обучение"; override val settingGoal = "Цель дня"; override fun goalValue(xp: Int) = "$xp XP в день"
    override val weekTitle = "Неделя"; override fun weekTotal(xp: Int) = "$xp XP за неделю"
    override val weekdays = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
    override val ranks = listOf("Новичок", "Ученик", "Практик", "Знаток", "Мастер", "Эксперт", "Наставник", "Гуру", "Легенда", "Чемпион")
    override val badgesTitle = "Награды"; override fun badgesCount(have: Int, total: Int) = "$have из $total"
    override val badgeNames = listOf("Первые шаги", "Первая тема", "Три дня подряд", "Коллекционер", "Сто очков", "Меткий глаз", "Неделя огня", "Пять тем", "Полтысячи", "Месяц без пропусков")
    override val badgeHints = listOf("Наберите 10 XP", "Пройдите тему целиком", "Занимайтесь 3 дня подряд", "Переверните 25 карточек", "Наберите 100 XP", "Ответьте верно на 20 вопросов", "Занимайтесь 7 дней подряд", "Пройдите 5 тем", "Наберите 500 XP", "Занимайтесь 30 дней подряд")
    override fun nextBadge(name: String, have: Int, target: Int) = "Следующая награда — «$name»: $have из $target"
    override val statTopics = "Тем пройдено"; override val statCards = "Карточек"; override val statAnswers = "Верных ответов"; override val quoteMore = "Нажмите, чтобы сменить"
    override val quoteTitle = "Мысль дня"
    override val quotes = listOf(
        "Маленький шаг каждый день сильнее большого рывка раз в месяц.",
        "Не обязательно знать всё. Достаточно сегодня узнать чуть больше, чем вчера.",
        "Ошибка — это не провал, а подсказка, куда смотреть дальше.",
        "Дорогу осилит идущий.",
        "Тот, кто начал, уже впереди тех, кто думает о начале.",
        "Привычка работает лучше мотивации: сделайте занятие частью дня.",
        "Сложное становится простым после десятого повтора.",
        "Не сравнивайте себя с другими — сравнивайте с собой вчерашним.",
        "Терпение и труд всё перетрут.",
        "Хороший день начинается с одной выученной вещи.",
        "Прогресс не всегда заметен, но он всегда накапливается.",
        "Главное — не скорость, а то, что вы не останавливаетесь."
    )
    override val tipTitle = "Совет дня"
    override val tips = listOf(
        "10 минут каждый день работают лучше, чем 2 часа раз в неделю.",
        "Перед ответом попробуйте вспомнить его сами — так запоминается крепче.",
        "Произносите слова вслух: озвучка карточек поможет с произношением.",
        "Повторите вчерашнюю тему перед тем, как начать новую.",
        "Придумайте свой пример к каждой карточке — со своим примером слово не забудется.",
        "Занимайтесь в одно и то же время: привычка закрепляется быстрее.",
        "Устали — сделайте перерыв. Мозг запоминает и во время отдыха."
    )

    override val continueLabel = "Продолжить"
    override val mapEmptyTitle = "Пока нет тем"; override val mapEmptyHint = "Скачайте темы в настройках или поставьте файл: json темы или zip с темами."
    override val openSettings = "Открыть настройки"; override val noGroup = "Темы"
    override fun groupProgress(done: Int, total: Int) = "$done из $total ${ruPlural(total, "темы", "тем", "тем")}"
    override fun cardsCount(n: Int) = "$n ${ruPlural(n, "карточка", "карточки", "карточек")}"

    override val stageTheory = "Теория"; override val stageCards = "Карточки"; override val stagePractice = "Практика"
    override val previous = "Назад"; override val next = "Дальше"; override val finish = "Готово"; override val speak = "Озвучить"
    override val tapToFlip = "Нажмите на карточку, чтобы перевернуть"
    override val checkPronunciation = "Проверить произношение"; override val pronListening = "Говорите…"
    override val pronNoSpeech = "Не расслышали. Нажмите на микрофон и повторите."
    override val pronNoPermission = "Нужен доступ к микрофону. Разрешите его в настройках телефона."
    override val pronFailed = "Распознавание речи недоступно. Проверьте сеть, службу распознавания речи и пакет языка в настройках."
    override fun pronGreat(score: Int) = "Отлично! $score%"
    override fun pronGood(score: Int) = "Почти! $score% — попробуйте ещё раз"
    override fun pronRetry(score: Int) = "Пока непохоже: $score%. Послушайте и повторите"
    override fun pronHeard(heard: String) = "Услышали: «$heard»"
    override val pronNoPack = "Для этого языка нет пакета распознавания речи. Скачайте его в Настройки → Озвучка и распознавание."
    override val tileVowel = "гласные"; override val tileConsonant = "согласные"
    override val listenPrompt = "Послушайте и выберите, что услышали"; override val speakPrompt = "Произнесите вслух"
    override val dictationPrompt = "Послушайте и впишите, что услышали"; override val typeAnswer = "Ваш ответ"
    override val check = "Проверить"; override val skip = "Пропустить"; override val speakSkipped = "Ничего страшного, пойдём дальше"
    override val bankPlaceholder = "Нажимайте на слова внизу, чтобы собрать ответ"
    override val matchPrompt = "Соедините пары"; override val matchHint = "Нажмите слева, потом справа"; override val matchWrong = "Пары собраны, но с ошибками"
    override fun cardCounter(index: Int, total: Int, seen: Int) = "$index из $total · просмотрено $seen"
    override val correct = "Верно!"
    override fun wrongAnswer(answer: String) = "Правильно: $answer"
    override fun quizResult(ok: Int, total: Int) = "Верно $ok из $total"
    override val retry = "Пройти снова"; override val stageDone = "Этап пройден"; override val topicDone = "Тема пройдена!"; override val seeResult = "Результат"

    override fun levelTitle(level: Int) = "Уровень $level"
    override fun xpToNext(xp: Int) = "До следующего уровня: $xp XP"
    override fun streakDays(n: Int) = "$n ${ruPlural(n, "день", "дня", "дней")} подряд"
    override fun topicsDone(done: Int, total: Int) = "Пройдено тем: $done из $total"
    override val xpRulesTitle = "Как получить XP"
    override val xpRules = "Теория +10 · просмотренная карточка +2 · верный ответ +5 · тема целиком +50. За повтор уже пройденного XP не даётся."
    override val sealsTitle = "Печати уровней"

    override val sectionLook = "Внешний вид"; override val settingTheme = "Тема"; override val settingLanguage = "Язык приложения"; override val settingAccent = "Акцентный цвет"
    override fun themeName(mode: ThemeMode) = if (mode == ThemeMode.DARK) "Тёмная (AMOLED)" else "Светлая"

    override val sectionData = "Данные"; override val resetProgress = "Сбросить прогресс"
    override val resetProgressHint = "Обнулит серию дней и пройденные этапы. XP, уровень и темы останутся."
    override val resetQuestion = "Сбросить прогресс? Пройденные этапы и серия удалятся, вернуть их будет нельзя. XP и уровень сохранятся."; override val reset = "Сбросить"

    override val sectionTopics = "Темы"; override val catalogAddress = "Адрес каталога"; override val catalogNotSet = "Не задан"
    override val catalogHint = "Адрес https-папки, где лежит course.json. Темы скачиваются по одной, каждая проверяется по контрольной сумме."
    override val refreshCatalog = "Обновить каталог"; override val fromFile = "Из файла"; override val downloadAll = "Скачать все"; override val download = "Скачать"
    override val availableTitle = "Доступные темы"; override val installedTitle = "Установленные темы"; override val remove = "Удалить"; override val removeAll = "Удалить все темы"
    override val catalogLoading = "Загружаем каталог…"; override val catalogFailed = "Не удалось получить каталог. Проверьте адрес и сеть."
    override val downloadFailed = "Не все темы скачались. Попробуйте ещё раз."; override val fileFailed = "Файл не похож на тему: нужен json темы или zip с темами."
    override val nothingNew = "Все темы из каталога уже установлены."
    override fun removeQuestion(title: String) = "Удалить тему «$title»? Прогресс сохранится."
    override fun removeAllQuestion(count: Int) = "Удалить все темы ($count)? Прогресс сохранится, темы можно поставить снова."
    override fun removeManyQuestion(count: Int) = "Удалить темы ($count)? Прогресс сохранится."
    override val mediaMissing = "Файл не скачан"
    override val noCourse = "Без курса"
    override val cancel = "Отмена"; override val save = "Сохранить"

    override val sectionVoice = "Озвучка и распознавание"
    override val voiceIntro = "Озвучка и проверка произношения работают на встроенных средствах Android. Здесь можно выбрать голос, скорость речи и скачать нужные пакеты."
    override val voiceRate = "Скорость речи"; override val rateSlow = "Медленно"; override val rateNormal = "Обычно"; override val rateFast = "Быстро"
    override val voiceAuto = "Авто"; override val voiceAutoHint = "Голос, который система выбрала сама"
    override val voiceHigh = "высокое качество"; override val voiceNetwork = "нужен интернет"; override val voiceNotInstalled = "не скачан"
    override val voicesLoading = "Ищем голоса…"; override val voicesMore = "Скачать другие голоса…"; override val voiceLanguage = "Язык"; override val voiceLabel = "Голос"
    override val packOn = "Скачан"; override val packOff = "Не скачан"
    override val recogTitle = "Распознавание речи"
    override val recogHint = "Для проверки произношения. С пакетом языка работает без интернета."
    override val packInstalled = "Пакет скачан, работает без интернета"; override val packAvailable = "Пакет не скачан, нужен интернет"
    override val packDownloading = "Скачивается…"; override val packUnsupported = "Управляется системой — откройте настройки голосового ввода"
    override val packUnknown = "Состояние неизвестно — проверьте в настройках голосового ввода"

    override val aboutTitle = "DL Studio"; override val aboutText = "Курсы по темам: теория, карточки и практика. Работает без интернета."
    override val aboutCopyright = "© 2026 DataLaw DesignLab. Все права защищены."
}
