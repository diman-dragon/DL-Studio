import { state } from '../state/appState.js';
import finishQuiz from '../study/finishQuiz.js';
/** Вариант ответа (data-i — номер). */
export default async function pick(d) {
  const q = state.quiz;
  if (!q || !q.kind.endsWith('-choice') || q.result) return;
  q.picked = Number(d.i);
  return finishQuiz(q.options[q.picked] === q.answer);
}
