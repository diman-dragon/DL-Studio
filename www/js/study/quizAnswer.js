/** Правильный ответ задания: пропущенное слово (cloze), перевод (на слух и направление «вперёд») или изучаемое слово. */
export default function quizAnswer(card, family, dir) {
  return family === 'cloze' ? card.cloze_answer : family === 'audio' || dir === 'fwd' ? card.back : card.front;
}
