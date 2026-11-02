import quizAnswer from './quizAnswer.js';
/** Чужие правильные ответы того же вида для вариантов выбора: без пустых, без повторов и без ответа самой карточки. */
export default function quizPool(session, card, family, dir) {
  const own = quizAnswer(card, family, dir);
  return [...new Set([...session.map.values()].filter(c => c.id !== card.id).map(c => quizAnswer(c, family, dir)))].filter(v => v && v !== own);
}
