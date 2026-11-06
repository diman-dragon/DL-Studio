package com.dlstudio.app.i18n

import com.dlstudio.app.settings.AccentColor
import com.dlstudio.app.settings.StorageStatus
import com.dlstudio.app.settings.ThemeMode

object StringsRu : Strings {
    override val tabStudy = "Учёба"
    override val tabStats = "Статистика"
    override val tabLibrary = "Библиотека"
    override val tabSettings = "Настройки"
    override val stubHint = "Раздел в разработке"
    override val settingTheme = "Тема"
    override val settingLanguage = "Язык приложения"
    override val settingAccent = "Акцентный цвет"
    override val settingFolder = "Папка хранения"
    override val settingAbout = "О нас"
    override val folderDefault = "Папка приложения"
    override val aboutTitle = "DL Studio"
    override val aboutText = "Карточки для изучения языков. Работает без интернета."
    override val close = "Закрыть"
    override val viewCards = "Карточки"
    override val viewList = "Список"
    override val edit = "Изменить"
    override val delete = "Удалить"
    override val save = "Сохранить"
    override val cancel = "Отмена"
    override val cardBack = "Обратная сторона"
    override val importFormat = "Одна карточка в строке: слово и перевод через «;», табуляцию или « - », например «cat;кот». Подходят CSV, TSV, TXT (в том числе экспорт Anki и Quizlet) и JSON. Первая строка «слово;перевод;колода» разложит карточки по колодам."
    override val importPick = "Выбрать файл"
    override val importEmpty = "В файле не нашлось карточек. Нужны строки вида «слово;перевод»."
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
