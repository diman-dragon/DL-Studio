export default function jsonFormat() {
  return `<pre class="format-code">{
  "format": "lingvadesk-library",
  "version": 1,
  "native_language": "ru",
  "projects": [{
    "name": "English",
    "tts": "en-US",
    "decks": [{
      "name": "Basics",
      "cards": [{
        "front": "apple",
        "back": "яблоко",
        "example": "I eat an apple.",
        "example_translation": "Я каждый день ем яблоко."
      }]
    }]
  }]
}</pre>`;
}
