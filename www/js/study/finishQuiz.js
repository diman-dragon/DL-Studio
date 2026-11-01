import { state } from '../state/appState.js';
import toggleFlip from '../dom/toggleFlip.js';
import buzz from '../dom/buzz.js';
import trackAnswer from './trackAnswer.js';
import autoSpeak from './autoSpeak.js';
import fitFaces from '../dom/fitFaces.js';
import quizControls from '../components/quizControls.js';
import quizVerdict from '../components/quizVerdict.js';
/** Ответ дан: закрываем отсчёт времени, переворачиваем карточку к ответу, показываем итог и «Дальше». */
export default async function finishQuiz(ok) {
  const q = state.quiz;
  q.result = ok ? 'ok' : 'bad';
  trackAnswer(ok, q.hint > 0);
  toggleFlip();
  document.getElementById('verdict').innerHTML = quizVerdict(q);
  document.getElementById('controls').innerHTML = quizControls(q);
  fitFaces();
  buzz(ok ? 12 : [30, 40, 30]);
  await autoSpeak('reveal');
}
