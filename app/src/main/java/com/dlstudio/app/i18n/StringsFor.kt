package com.dlstudio.app.i18n

import com.dlstudio.app.settings.AppLang

fun stringsFor(lang: AppLang): Strings = if (lang == AppLang.RU) StringsRu else StringsEn
