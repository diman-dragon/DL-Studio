import esc from '../util/esc.js';
/** Итог вопроса на обороте карточки: верно / неверно (и что ввели). */
export default function quizVerdict(q) {
  if (!q?.result) return '';
  if (q.result === 'ok') return '<span class="verdict ok">✓ Верно</span>';
  return `<span class="verdict bad">✗ Неверно${q.typed ? ` · вы ввели: ${esc(q.typed)}` : ''}</span>`;
}
