/** Вид задания случайно: показать слово (visual), на слух (audio); у карточки с пропуском — ещё cloze. */
export default function quizFamily(card) {
  const families = card.cloze ? ['visual', 'audio', 'cloze'] : ['visual', 'audio'];
  return families[Math.floor(Math.random() * families.length)];
}
