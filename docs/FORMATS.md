# Форматы

## Библиотека — `lingvadesk-library` v1 (экспорт и импорт на верхнем уровне библиотеки)
```json
{
  "format": "lingvadesk-library",
  "version": 1,
  "projects": [
    {
      "name": "English", "tts": "en-US",
      "decks": [
        { "name": "Basics", "cards": [
          { "front": "apple", "back": "яблоко", "example": "I eat an apple.", "example_translation": "Я ем яблоко." }
        ] }
      ]
    }
  ]
}
```
- **Проект** — язык, тема, даты, что угодно. `tts` — голос озвучки (BCP-47, по умолчанию `en-US`).
- Обязательно: `name` у проекта и колоды, `front` и `back` у карточки (невалидные карточки пропускаются). `example`, `example_translation` — по желанию.
- Порядок карточек в массиве = порядок в колоде.
- **Импорт сливает**: проекты и колоды ищутся по имени (нет — создаются), карточка добавляется, только если в колоде нет такой же пары «слово + перевод». Повторный импорт ничего не дублирует.

## Колода — `flashcards-deck` v1 (импорт одной колоды)
```json
{ "format": "flashcards-deck", "version": 1,
  "language": { "name": "English", "tts": "en-US" },
  "deck": { "name": "Basics" },
  "cards": [ { "front": "apple", "back": "яблоко" } ] }
```
`language` — это проект. Файл импортируется как библиотека из одного проекта и одной колоды.

Примеры: `examples/deck.json`, `examples/library.json`. Кодировка UTF-8.

## .fcbak
ZIP-архив:
- `manifest.json`: `{ "format": "fcbak", "version": 1, "dbVersion": 1, "created": <мс> }`
- `app.db`: файл SQLite

Копии с `dbVersion` выше поддерживаемой отклоняются.
