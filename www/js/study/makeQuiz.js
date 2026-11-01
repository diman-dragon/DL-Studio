import shuffle from '../util/shuffle.js';
/**
 * Вопрос с выбором или вводом (настройка «Проверка»); null — обычный переворот или нечего выбирать.
 * Варианты — ответы других карточек области. dir — какая сторона отвечает: 'fwd' — перевод, 'rev' — слово.
 * «На слух» — всегда выбор из переводов (как в игре), независимо от настройки «Проверка»: слово звучит, надо выбрать верный перевод.
 */
export default function makeQuiz(session, card, dirPref, checkPref) {
  const listen = session.mode === 'listen', dir = listen ? 'fwd' : dirPref, check = listen ? 'choice' : (checkPref === 'mix' ? ['flip', 'choice', 'type', 'speak'][Math.floor(Math.random() * 4)] : checkPref);
  if (check === 'flip' || session.mode === 'cards') return null;
  if (check === 'speak') return { id: card.id, kind: 'speak', result: null, hint: 0, typed: '' };
  const text = c => (dir === 'fwd' ? c.back : c.front);
  const answer = text(card), base = { id: card.id, answer, result: null, hint: 0, typed: '' };
  if (check === 'type') return { ...base, kind: 'type' };
  const pool = [...new Set([...session.map.values()].filter(c => c.id !== card.id).map(text))].filter(t => t !== answer);
  if (!pool.length) return null;
  return { ...base, kind: 'choice', options: shuffle([answer, ...shuffle(pool).slice(0, 3)]), picked: null };
}
