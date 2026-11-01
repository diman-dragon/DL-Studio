import { state } from '../state/appState.js';
import quizControls from '../components/quizControls.js';
/** «Подсказка»: ещё одна буква ответа в поле; ответ с подсказкой оценивается как «трудно». */
export default function hint() {
  const q = state.quiz;
  if (!q || q.kind !== 'type' || q.result) return;
  q.typed = document.getElementById('ans').value;
  q.hint = Math.min(q.hint + 1, q.answer.length);
  document.getElementById('controls').innerHTML = quizControls(q);
  document.getElementById('ans')?.focus?.();
}
