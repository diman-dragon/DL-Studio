/** Полный текст для озвучки: пропуск в cloze заполняется правильным словом. */
export default function spokenText(card) {
  if (!card.cloze) return card.front;
  return card.cloze.replace(/_{3,}|…/g, card.cloze_answer || '');
}
