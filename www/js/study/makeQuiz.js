import shuffle from '../util/shuffle.js';

/** Случайное задание SRS: переворот, выбор, ввод или произношение. */
export default function makeQuiz(session, card, dir, requested = null) {
  // requested оставлен для чистых unit-тестов и совместимости API; интерфейс настройку больше не показывает.
  const check = requested || ['flip', 'choice', 'type', 'speak'][Math.floor(Math.random() * 4)];
  if (check === 'flip') return null;
  if (check === 'speak') return { id: card.id, kind: 'speak', result: null, hint: 0, typed: '' };
  const text = c => (dir === 'fwd' ? c.back : c.front);
  const answer = text(card), base = { id: card.id, answer, result: null, hint: 0, typed: '' };
  if (check === 'type') return { ...base, kind: 'type' };
  const pool = [...new Set([...session.map.values()].filter(c => c.id !== card.id).map(text))].filter(t => t !== answer);
  if (!pool.length) return null;
  return { ...base, kind: 'choice', options: shuffle([answer, ...shuffle(pool).slice(0, 3)]), picked: null };
}
