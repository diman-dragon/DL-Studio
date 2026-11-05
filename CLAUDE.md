# Для Claude
Проект — нативный Android (Kotlin/Compose), версия в `package.json`. Домен (`domain/`) — чистый Kotlin без Android; его тесты — `android/app/src/test`.
После изменений: поднимай версию в `package.json` + `CHANGELOG.md`, `applicationId`, ключ подписи и схему БД v6 (`data/local/LingvaDatabase.kt`) не менять без миграции.
