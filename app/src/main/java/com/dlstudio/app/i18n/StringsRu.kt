package com.dlstudio.app.i18n

import com.dlstudio.app.progress.DayPart
import com.dlstudio.app.progress.GoalPhase
import com.dlstudio.app.progress.Mood
import com.dlstudio.app.content.QuizKind
import com.dlstudio.app.settings.ThemeMode
import com.dlstudio.app.speech.How

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

    override fun goalNudge(phase: GoalPhase, left: Int) = when (phase) {
        GoalPhase.START -> "Начните с одной карточки: это сразу +2 XP"
        GoalPhase.EARLY -> "Ещё $left XP — это примерно ${(left + 4) / 5} ${ruPlural((left + 4) / 5, "верный ответ", "верных ответа", "верных ответов")}"
        GoalPhase.HALF -> "Больше половины позади! Осталось $left XP"
        GoalPhase.CLOSE -> "Совсем чуть-чуть: ещё $left XP!"
        GoalPhase.DONE -> "Всё, что сверху, — бонус к уровню"
    }
    override val cheers = listOf(
        "Вы справитесь. Я в вас верю! 💪",
        "Одна тема — и можно со спокойной душой отдыхать.",
        "Каждое слово — кирпичик. Строим дальше!",
        "Повторение — мать учения. Хотите, повторим пару карточек?",
        "Серия сама себя не продлит 😉",
        "Пять минут сегодня — и вы уже впереди себя вчерашнего.",
        "Отличный день, чтобы выучить что-то новое!",
        "Ошибаться — нормально. Так и запоминается."
    )
    override val questsTitle = "Задания"; override val questGoal = "Набрать цель дня"
    override fun questBadge(name: String) = "Награда «$name»"; override fun questLevel(level: Int) = "Дойти до уровня $level"
    override val pathTitle = "Ваш путь"

    override val continueLabel = "Продолжить"
    override val collapseAll = "Свернуть всё"; override val expandAll = "Развернуть всё"
    override fun lastSeen(title: String) = "Остановились: «$title»"
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
    override val pronNoPack = "Для этого языка нет пакета распознавания речи."
    override val tileVowel = "гласные"; override val tileConsonant = "согласные"
    override val listenPrompt = "Послушайте и выберите, что услышали"; override val speakPrompt = "Произнесите вслух"
    override val dictationPrompt = "Послушайте и впишите, что услышали"; override val typeAnswer = "Ваш ответ"
    override val check = "Проверить"; override val skip = "Пропустить"; override val speakSkipped = "Ничего страшного, пойдём дальше"
    override val bankPlaceholder = "Нажимайте на слова внизу, чтобы собрать ответ"
    override val matchPrompt = "Соедините пары"; override val matchHint = "Нажмите любой вариант, потом его пару с другой стороны"; override val matchWrong = "Пары собраны, но с ошибками"
    override fun cardCounter(index: Int, total: Int, seen: Int) = "$index из $total · просмотрено $seen"
    override val correct = "Верно!"
    override fun wrongAnswer(answer: String) = "Правильно: $answer"
    override fun quizResult(ok: Int, total: Int) = "Верно $ok из $total"
    override fun quizKind(kind: QuizKind, dictation: Boolean) = when {
        dictation -> "Диктант"
        kind == QuizKind.CHOICE -> "Выбери ответ"; kind == QuizKind.LISTEN -> "Послушай и выбери"; kind == QuizKind.SPEAK -> "Скажи вслух"
        kind == QuizKind.BANK -> "Собери фразу"; kind == QuizKind.FILL -> "Впиши ответ"; else -> "Соедини пары"
    }
    override fun resultHeadline(good: Boolean) = if (good) "Отличный результат!" else "Можно лучше — ещё раз?"
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

    override val sectionTopics = "Темы"; override val fromFile = "Добавить из файла"; override val download = "Скачать"
    override val installedTitle = "Установленные темы"; override val remove = "Удалить"; override val removeAll = "Удалить все темы"
    override val fileFailed = "Файл не похож на тему: нужен json темы или zip с темами."
    override fun removeQuestion(title: String) = "Удалить тему «$title»? Прогресс сохранится."
    override fun removeAllQuestion(count: Int) = "Удалить все темы ($count)? Прогресс сохранится, темы можно поставить снова."
    override fun removeManyQuestion(count: Int) = "Удалить темы ($count)? Прогресс сохранится."
    override val mediaMissing = "Файл не скачан"
    override val noCourse = "Без курса"
    override val cancel = "Отмена"; override val save = "Сохранить"

    override val sectionVoice = "Языки и озвучка"
    override val voiceIntro = "Языки берутся из установленных тем. Озвучка и проверка произношения работают на встроенных средствах Android: здесь можно скачать нужные голоса и пакеты, выбрать голоса и скорость речи."
    override val voiceRate = "Скорость речи"; override val rateSlow = "Медленно"; override val rateNormal = "Обычно"; override val rateFast = "Быстро"
    override val voiceAuto = "Авто"
    override val voiceHigh = "высокое качество"; override val voiceNetwork = "нужен интернет"; override val voiceNotInstalled = "не скачан"
    override val voicesLoading = "Ищем голоса…"; override val voicesMore = "Скачать другие голоса…"; override val voiceLanguage = "Язык"
    override val packOn = "Скачан"; override val packOff = "Не скачан"
    override val recogTitle = "Распознавание речи"
    override val recogHint = "Для проверки произношения. С пакетом языка работает без интернета."
    override val packInstalled = "Пакет скачан, работает без интернета"; override val packAvailable = "Пакет не скачан, нужен интернет"
    override val packDownloading = "Скачивается…"; override val packUnsupported = "Состояние пакета определить нельзя: если он установлен, проверка произношения работает"
    override val packUnknown = "Состояние неизвестно: если пакет установлен, проверка произношения работает"

    override val aboutTitle = "DL Studio"; override val aboutText = "Курсы по темам: теория, карточки и практика. Работает без интернета."
    override val aboutCopyright = "© 2026 DataLaw DesignLab. Все права защищены."

    override val genderFemale = "женский"; override val genderMale = "мужской"
    override val statusRecog = "Распознавание"
    override val statusNoLangs = "Установите тему, и здесь появятся её языки, голоса и пакеты."
    override val downloadNeeded = "Скачать недостающее"; override val allReady = "Всё нужное установлено"
    override val offerTitle = "Нужны языковые пакеты"; override val later = "Позже"
    override fun offerText(languages: String) = "Для установленных тем не хватает голосов или пакетов распознавания: $languages. Скачать недостающее?"
    override val charactersTitle = "Персонажи"
    override val genderAutoHint = "Голос подберётся сам и закрепится за персонажем"
    override val voiceGenderHint = "В списках только голоса с явно известным полом (по рекомендациям Google). Если подходящего голоса нет, персонаж говорит голосом системы или голосом, общим с другим персонажем, но со своим тембром и скоростью."
    override val statusFemale = "Женский голос"; override val statusMale = "Мужской голос"

    override fun characterName(id: String) = when (id) { "grace" -> "Мисс Грейс"; "leo" -> "Мистер Лео"; "beatrice" -> "Беатрис"; "john" -> "Джон"; else -> "Диктор" }
    override fun characterRole(id: String) = when (id) { "grace" -> "Преподавательница"; "leo" -> "Преподаватель"; "beatrice" -> "Ученица"; "john" -> "Ученик"; else -> "Диктор" }
    override fun characterBio(id: String) = when (id) {
        "grace" -> "Строгая, но справедливая: очки, деловой воротничок, волосы до плеч. Объясняет правила чётко и по делу, ведёт урок."
        "leo" -> "Молодой энтузиаст с бородой, в горчичном свитере. Объясняет на примерах, шутит и поддерживает."
        "beatrice" -> "Любознательная студентка с фиолетовой стрижкой. Задаёт вопросы, ошибается и учится вместе с вами."
        "john" -> "Спокойный парень в очках. Отвечает, уточняет и разыгрывает диалоги."
        else -> "Невозмутимый голос за кадром: читает инструкции и задания."
    }
    override fun voiceHow(how: How) = when (how) {
        How.CHOSEN -> "голос выбран вами"
        How.OWN -> "свой голос, закреплён"
        How.SHARED -> "голос общий с другим персонажем, тембр и скорость свои"
        How.DEFAULT -> "голос системы, тембр и скорость свои"
    }
    override val sayHello = "Поздороваться"; override val introAgain = "Показать знакомство заново"
    override val introAgainHint = "Героев курсов и первые появления персонажей представят ещё раз."
    override val portraitsCredit = "Портреты персонажей: Micah, автор Micah Lanier (DiceBear), лицензия CC BY 4.0."
    override fun castTitle(course: String) = "Герои курса «$course»"
    override val castText = "Эти персонажи ведут уроки и разыгрывают диалоги. У каждого свой голос и свой портрет: нажмите на портрет, чтобы услышать."
    override val castStart = "К урокам"
    override fun meetTitle(name: String) = "Знакомьтесь: $name"
    override val dialogueListen = "Слушать диалог"; override val dialogueStop = "Остановить"
    override val problemNoEngine = "Озвучки нет: на устройстве не найден синтезатор речи. Текст остаётся на экране, персонажи говорят без звука."
    override val problemNoLanguage = "Нет голоса для языка темы, поэтому звука нет. Скачайте голос, и персонажи заговорят."
    override val problemOk = "Понятно"
    override val tabOverview = "Обзор"; override val tabStats = "Статистика"; override val downloadPack = "Скачать пакет распознавания"
    override fun voiceCounts(installed: Int, known: Int) = "Скачано голосов: $installed, из них с известным полом: $known"
    override val statsEmpty = "Пока нечего показать: решайте задания, листайте карточки и теорию, и здесь появится статистика."
    override val statsRight = "Верно"; override val statsWrong = "Неверно"; override val statsAccuracy = "Точность"
    override val statsCards = "Карточек просмотрено"; override val statsPages = "Страниц теории"; override val statsListens = "Прослушиваний"
    override val statsAnswers = "Ответы"; override val statsXpDays = "XP по дням (14 дней)"; override val statsAnswerDays = "Ответы по дням (14 дней)"
    override val statsCardDays = "Карточки по дням (14 дней)"; override val statsByKind = "По видам заданий"; override val statsByTopic = "По темам"
    override val statsPron = "Произношение"; override val statsTries = "Попыток"; override val statsAvg = "Средняя оценка"; override val statsBest = "Лучшая оценка"
    override val statsGreat = "Отлично"; override val statsGood = "Хорошо"; override val statsRetry = "Ещё раз"
    override val kindChoice = "Выбор"; override val kindListen = "На слух"; override val kindSpeak = "Произнести"
    override val kindBank = "Мешок слов"; override val kindFill = "Вписать"; override val kindMatch = "Пары"
}
