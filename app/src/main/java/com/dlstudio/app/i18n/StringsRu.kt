package com.dlstudio.app.i18n

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
    override val settingFolder = "Папка хранения"
    override val settingAbout = "О нас"
    override val folderDefault = "Папка приложения"
    override val aboutTitle = "DL Studio"
    override val aboutText = "Карточки для изучения языков. Работает без интернета."
    override val close = "Закрыть"
    override fun themeName(mode: ThemeMode) = if (mode == ThemeMode.DARK) "Тёмная (AMOLED)" else "Светлая"
    override fun status(status: StorageStatus) = when (status) {
        StorageStatus.NONE -> ""
        StorageStatus.MOVING -> "Переношу файлы…"
        StorageStatus.MOVED -> "Готово: файлы перенесены, старая папка удалена"
        StorageStatus.ERROR_INSIDE -> "Нельзя выбрать папку внутри DL-Studio"
        StorageStatus.ERROR_IO -> "Не удалось перенести, данные остались на старом месте"
    }
}
