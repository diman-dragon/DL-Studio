import { state } from '../state/appState.js';
import readField from '../dom/readField.js';
import toast from '../dom/toast.js';
import sameAnswer from '../util/sameAnswer.js';
import finishQuiz from '../study/finishQuiz.js';
/** «Проверить» (и Enter в поле ввода). */
export default async function check() {
  const q = state.quiz;
  if (!q || !q.kind.endsWith('-type') || q.result) return;
  const typed = readField('ans');
  if (!typed) return toast('Введите ответ');
  q.typed = typed;
  return finishQuiz(sameAnswer(typed, q.answer));
}
