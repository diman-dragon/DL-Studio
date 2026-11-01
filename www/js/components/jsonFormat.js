export default function jsonFormat() {
  return `<pre class="format-code">{
  "format": "lingvadesk-library",
  "version": 1,
  "native_language": "ru",
  "projects": [{
    "name": "Название проекта",
    "tts": "код-языка",
    "decks": [{
      "name": "Название колоды",
      "cards": [{
        "front": "изучаемое слово или фраза",
        "back": "перевод на нативный язык",
        "example": "необязательный пример",
        "example_translation": "перевод примера"
      }]
    }]
  }]
}</pre>`;
}
