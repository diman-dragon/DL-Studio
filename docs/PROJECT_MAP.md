# Карта проекта LingvaDesk 3.0.0 (Kotlin / Compose)

Пакет `com.lingvadesk.app` (`android/app/src/main/java/com/lingvadesk/app`), `applicationId` — `com.example.lingocards`.

- `domain/model` — модели; `domain/srs` — `Srs` (nextSrs), `GradeInference` (inferGrade, speechGrade), `SpeechScore`;
  `domain/study` — `Prefs` (настройки, лимиты), `SessionPlanner` (план), `Quiz` (задания), `Streaks`; `domain/repository` — интерфейс.
- `data/local/LingvaDatabase` — SQLite `lingocards-clean.db`, схема v6 + миграции; `data/repository` — запросы;
  `data/transfer` — импорт/экспорт JSON (`LibraryTransfer`), резервная копия `.fcbak` (`BackupManager`).
- `platform` — озвучка `TtsEngine`, значок `icon/*`.
- `presentation` — тема, навигация (Учёба → Статистика → Библиотека → Настройки), экраны `study/`, `stats/`, `library/`, `settings/`.
- `di` — Hilt. Тесты домена — `android/app/src/test`. Форматы файлов — `docs/FORMATS.md`.
