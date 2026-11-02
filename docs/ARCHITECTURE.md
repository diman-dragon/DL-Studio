# Архитектура LingvaDesk

**Модель.** Проект (язык, даты — что угодно) → Колода → Карточка. В БД проект хранится в таблице `languages`
(имя сохранено ради обновления поверх без миграции), в коде и интерфейсе — «проект» (`project`).

## Экраны и навигация
| Экран | Где | Назначение |
|---|---|---|
| `library` | вкладка «Библиотека» | проекты; **импорт и экспорт всей библиотеки** — только здесь |
| `project` → `deck` | библиотека | колоды проекта → карточки колоды (карточки живут только здесь) |
| `projectForm`, `deckForm`, `cardForm` | библиотека | создать / изменить / удалить / перенести |
| `study` | вкладка «Учёба» | область (вся библиотека / проект / колода), статистика, 3 режима |
| `session` | вкладка «Учёба» | сама учёба: интервальное повторение, обучение, повторение |
| `settings` | вкладка «Настройки» | один экран: плитки-переключатели (нажатие — следующее значение по кругу), данные, нижние окна «Формат JSON» и «О приложении» |

- **Нижняя панель** (Библиотека · Учёба) всегда на экране. Настройки — в шапке.
- **Без прокрутки.** Экран — flex-колонка на всю высоту (`100dvh`). Длинные списки выводятся страницами по высоте области
  (`pager/*`, размеры элементов — `constants/PAGER` и CSS `.item`/`.gtile`), страницы листаются свайпом и точками.
  Формы и карточки компактны; на низких экранах (`≤700px`, `≤600px`) CSS ужимается.
- **«Назад».** Собственный стек экранов (`state.stack`): каждый переход (`nav/go`) кладёт предыдущий экран в стек, «Назад»
  (`nav/popBack`) снимает верхний — ровно один шаг; удалённые экраны пропускаются. На первом экране (библиотека) остаёмся.
  Системную кнопку перехватывает Android: `MainActivity.onBackPressed` → `BackToJs` шлёт в WebView событие `lingvaBack`,
  его слушает `events/bindEvents` → `nav/systemBack`. Историю браузера (`pushState`) не используем: Chrome/WebView
  пропускает записи, созданные без жеста пользователя. Приложение кнопкой «Назад» не закрывается.
- **Кольцо свайпов.** Библиотека → Учёба → Настройки → Библиотека. Свайп листает страницы по кругу; за краем последней страницы
  верхнеуровневого экрана — переход к соседнему (`nav/swipeNavigate`, `nav/switchRing`). Внутри проекта/колоды страницы
  замыкаются сами на себя. В сессии учёбы, на формах свайп по области отключён.
- **Карточки по кругу.** Свайп по карточке: влево — следующая, вправо — предыдущая, после последней — первая (`study/cyclePos`).

## Учёба (`study/`)
- Принцип: **всё пишется в базу и читается из неё**. В `settings` — только настройки; сессия нигде не сохраняется (очередь строится заново из `cards`),
  «сегодня», серии и дневной лимит новых считаются из `reviews` (`study/loadToday`, `study/streaks`, `repo/countNewByDeck`).
- Вкладка «Учёба» («Давай-ка поучим…»): режимы — элементы списка / сетки (`components/modeEntry` → `entry`, переключатель `layoutToggle`, страницы `pagedList`, как в библиотеке); нажатие открывает экран `studyDecks` (`views/studyDecksView`): «Все колоды» и проекты (нажатие — колоды проекта: «Весь проект» + колоды) со счётчиками (`repo/listDeckStats`, `study/planInfo`) в том же списке / сетке; нажатие на колоду запускает режим (`actions/pickScope`).
  Область сессии — `'all' | 'p:<id>' | 'd:<id>'` (`repo/scopeClause`).
- `srs` — интервальное повторение: просроченные (давние первыми) + новые, **не больше `newPerDay` на каждую колоду** (`study/buildOrder`, `study/loadLimits`), всё не больше `sessionMax`.
- `listen` — «На слух»: `views/listenFace` (плеер, спрятанный текст, `actions/showText`, `actions/play`), давно не слушанные первыми (`ll` в `repo/listCardsInScope`); пишет только историю.
- `speak` — «Говорить» (проверка произношения): `views/speakFace` (перевод + микрофон), `actions/record` → `native/speechListen` → плагин `speech/SpeechListenPlugin`
  (Android `SpeechRecognizer`, сервисы Google, офлайн-пакет предпочтителен, язык — голос проекта). `study/scoreSpeech`: сходство услышанного с `card.front`
  (лучший из вариантов, по символам, уверенность слегка смещает), `srs/speechGrade`: оценка карточки (границы — `constants/SPEECH`). Каждая попытка — строка в
  `pronunciations`; записи голоса **не сохраняются** (плагин возвращает `audio: null`, колонка и поле предусмотрены). Расписание не двигается, в `reviews` — режим `speak`.
  Другой движок (Vosk / Whisper) подключается заменой `native/speechListen`; чьё именно распознавание — в `pronunciations.engine`.
- Оценок кнопками нет: `srs/inferGrade` выводит оценку из поведения (`study/track*`: время до переворота и на ответе, перевороты, возвраты, прослушивания, подсказка);
  калибровка порогов — `srs/calibrate`; вопросы выбором / вводом — `study/makeQuiz`, `study/finishQuiz`. `study/gradeCard` пишет в `reviews` и (в `srs`) двигает SM-2 (`srs/nextSrs`);
  `study/undoGrade` откатывает последнюю оценку; «пиявки» — `study/checkLeech`.
- Статистика (`views/statsView` + `stats/*`): 12 параметров — элементы списка / сетки (`entry`), по нажатию подробное окно (`components/statSheet`, `state.sheet = 'stat:<n>'`); всё из `reviews`, `pronunciations` и `cards`.
- Сводка на режимах «Учёбы» (`components/studyInfo`): всего · изучено · на сегодня (4 градации цвета `study/loadLevel`) и красный «?», если есть просроченные (`listDeckStats.late` — срок раньше начала сегодняшних суток).
- Порядок карточек в сессии случайный (`study/buildOrder`) и в сессии не меняется. Алгоритм повторений работает один раз — при открытии сессии он составляет план (`study/takePlan`); дальше сессия идёт строго по плану, «Снова» не возвращается, сверх плана карточек нет. Оценки пишутся в базу и влияют на следующий план.
- Задания (`study/quizPick`): перевод, на слух, пропуск, произношение показываются равномерно, ввод с клавиатуры — в 4 раза реже; вид задания фиксируется на карточку на всю сессию.
- **Схема БД v3** (`db/Schema`, миграции `db/MigrateV2`, `db/MigrateV3`; тест `test/migration.test.js` сверяет, что миграция даёт ту же схему, что чистая установка).
  `reviews(card_id, ts, grade, think, look, fresh, mode, hinted, plays)`; `pronunciations(card_id, ts, engine, transcript, score, audio)` — заготовка под проверку произношения (позже).

## Значок приложения, игра, импорт
- **Значок** (`android/.../icon/*`, `AndroidManifest.xml`): четыре `activity-alias` (`.IconL0…3`, включён один), значки — адаптивные XML + PNG для API < 26 (`res/mipmap-*/ic_launcher_l1…3`).
  `IconLevel` — формула уровня (12 / 17 / 20 ч, «занимались сегодня», «есть что учить»), `IconQuery` читает базу, `IconApply` переключает алиас (`DONT_KILL_APP`),
  `IconSchedule` + `IconReceiver` — будильник раз в 30 минут и перезагрузка. JS: `study/syncIcon` (запуск, возвращение в приложение), `study/markStudied` (позанимались → уровень 0).
- **Игра** (`game/*`, `views/gameView`, таблица `games`, `reviews.mode = 'game'`): `newGame → nextQuestion → answerQuestion → answerGame → finishGame`; таймер — `game/tickGame` (DOM без перерисовки).
- **Импорт** (`deckio/*`): `readImportFile` (определяет по сигнатуре zip / по тексту) → `readImportText` (наш JSON → `parseImport`; чужой JSON → `parseJsonCards`; таблица → `parseTextRows` → `rowsToCards`) или `readZip`
  (`unzip` + `parseXlsx`; `.apkg` — коллекция уходит в плагин `anki/AnkiNotesPlugin`, потому что в JS нет SQLite). Экран «Импорт» — `views/importFormView`, `actions/confirmImport`.

## Страницы, вкладки, распознавание речи
- Нижняя панель — четыре вкладки (`constants/TABS`, `TAB_OF`); кольцо свайпов — `constants/RING`. Шапка (`components/header`) без служебных кнопок.
- Список делится на страницы по **реальной высоте** элементов (`pager/measureStarts` → `pager/pageStarts`; запасной вариант без измерений — `pager/uniformStarts`). Текст везде переносится (`overflow-wrap:anywhere`), строки растут (`min-height`).
- Распознавание: `SpeechListener` повторяет попытку онлайн при кодах 12 / 13; `study/prepareSpeech` + `SpeechSupport` проверяют и скачивают офлайн-пакет языка (Android 13+).

## Жесты и `touch-action`
Все свайпы — Pointer Events (`gestures/`). Чтобы WebView не отнимал жест (`pointercancel`), `touch-action: none` задан на `.view`,
на всех её потомках и на каждом прокручиваемом контейнере внутри (`.face`, `.pager`): Chromium считает эффективное
`touch-action` только до ближайшего прокручиваемого предка. Жест засчитывается, если путь ≥ 60 px или это быстрый «флик»
(≥ 32 px при скорости ≥ 0,35 px/мс); отменяется только явно вертикальный жест. Клик после свайпа гасится.

## Структура кода
```
www/js/
  main.js → boot.js        точка входа
  constants/ state/ util/ dom/
  native/                  обёртки над плагинами (db*, saf*, backup*, ttsSpeak, ttsStop)
  repo/                    по одному SQL-запросу на файл
  settings/                loadPrefs, savePref, setSetting, getSetting, applyPrefs, resolveTheme
  srs/                     nextSrs, nextInterval, nextEase, inferGrade, isNewCard
  study/                   startSession, buildOrder, restoreOrder, cyclePos, leaveCard, gradeCard, track*, pickDeck, loadToday, …
  deckio/                  parseImport, parseDeck, parseLibrary, saveLibrary, importLibraryFromFile, exportLibraryToFile, …
  pager/                   layoutPager, perPage, pageCount, setPage
  nav/                     go, popBack, systemBack, goBack, parentOf, render, swipeNavigate, switchRing
  gestures/                onPointer*, isSwipe, finishCardSwipe, finishAreaSwipe, flyOut, springBack, …
  components/ views/       разметка
  actions/                 по одному действию на файл + index.js
  events/                  dispatch, bindEvents, onResize, watchSystemTheme
android/…/java/com/example/lingocards/   плагины Capacitor (core, db, saf, backup, tts)
```
Правила кода — `docs/CONVENTIONS.md`; форматы — `docs/FORMATS.md`; обновление поверх — `docs/UPDATES.md`.

## Данные (SQLite `lingocards.db`, схема v1)
`languages` (проекты), `decks`, `cards` (+ SRS: `due`, `interval`, `ease`, `reps`), `settings` (настройки, область учёбы, позиции сессий),
`deck_state` (устарела, не используется; оставлена ради совместимости резервных копий). WAL выключен: БД — один файл.

## Backup / restore
`BuildFcbak`: ZIP {manifest.json, app.db}. `RestoreDb`: проверка (`IsValidDb`) → защитная копия → замена → проверка → при ошибке откат.

## Тесты
`npm test` (Node 22, `node:sqlite`): алгоритм SRS, сессии (круговое листание, фазы, области), импорт/экспорт библиотеки,
сквозной UI-сценарий (экраны, CRUD, страницы, кольцо свайпов, «Назад», жесты), версия и подпись, иконки.
Мост Capacitor подменяет `test/helpers/installBridge.js`, схема берётся из `Schema.java`.

## Текст помещается всегда
Карточки (`.face`) подгоняются `dom/fitFaces` (масштаб `--fit` для текста, отступов, кнопок озвучки; не мельче `FIT_MIN`); варианты ответа — `components/optGrid` (длинные — один столбец, мельче, прокрутка внутри `.opts`); карточка не ниже 150 px (`.flip`), колонки списков — `minmax(0,1fr)`, длинное слово переносится (`overflow-wrap:anywhere`). Проверка — `tools/qa-layout/`.
