# Карта проекта LingvaDesk

LingvaDesk — чистое офлайн-приложение для интервального повторения. Данные организованы как проект → колода → карточка.

## Навигация

Нижняя панель и кольцо экранов имеют один порядок: **Учёба → Статистика → Библиотека → Настройки**.

- `views/studyHubView.js` — проекты с количеством карточек, карточек к повторению и просроченных.
- `views/sessionView.js` — единственная учебная сессия SRS.
- `views/statsView.js` — статистика повторений и произношения.
- `views/libraryView.js` — CRUD проектов, колод и карточек, импорт и экспорт.
- `views/settingsView.js` — нативный язык, внешний вид, лимиты, формат JSON и данные.

## Учёба

`study/startSession.js` строит сессию заново из базы. `study/buildOrder.js` ставит просроченные и карточки плана первыми. `study/cardDir.js` случайно выбирает направление карточки и фиксирует его в сессии.

`study/makeQuiz.js` автоматически выбирает задание: переворот, варианты ответа, ввод с клавиатуры или произношение. `srs/inferGrade.js` и `srs/speechGrade.js` превращают поведение и результат произношения в оценку SRS.

## Формат JSON

Подробный формат находится в `docs/FORMATS.md`. В настройках показан рабочий пример с `native_language`, проектом, изучаемым языком `tts`, колодой и карточками `front`, `back`, `example`, `example_translation`.

## Слои

- `actions/` — действия интерфейса.
- `views/` и `components/` — HTML-представления.
- `study/`, `srs/`, `stats/` — логика обучения и статистики.
- `deckio/` — импорт и экспорт.
- `repo/` — SQL-запросы.
- `native/` — мост Capacitor и Android-плагины.

## Версия 2.3.0: что где
- Задание карточки на сессию: `study/taskFor.js` (кэш `session.tasks`), сборка — `study/makeQuiz.js` + `quizAnswer/quizCheck/quizChoices/quizFamily`.
- «На слух»: `components/audioPrompt.js`, `study/textLater.js` (кнопка через `BEHAVIOR.textAfter`), `actions/showText.js`.
- Замедленный повтор: `components/speakControls.js`, `slowButton.js`, `actions/replaySlow.js`, `tts/speakSlow.js`, `constants/SLOW.js`, `state/slowState.js`, `study/stopSlow.js`.
- Повторение колоды: `constants/DECK_PREFS|DECK_TITLES|DECK_DEFAULTS`, `settings/loadDeckPrefs|saveDeckPref|deckPrefOf`, `study/loadLimits|deckLimits|takePlan|paceOf`, `srs/nextSrs` (параметр `pace`), экран `views/studyDecksView.js` (без правок: только запуск), правка параметров — форма колоды в библиотеке, плитки `components/deckPrefTiles.js`.
- Настройки одним экраном: `views/settingsView.js`, `components/settingTiles|prefTile|dataButtons|settingsSheet`, `actions/cyclePref|cycleDeckPref|openSheet`, `util/nextOption.js`.
- Повтор ошибок: `study/retrySession|mistakesOf`, `actions/retryMistakes`; задания: `study/quizPick|quizPool|checkSpeech`; порядок новых: `study/orderFresh`; разброс: `constants/FUZZ`, `study/randomFuzz`.
- Слова не рвутся: `dom/fitWords.js` + `dom/wordBroken.js` (вызов из `dom/fitFaces`).
