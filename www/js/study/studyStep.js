import { state } from '../state/appState.js';
import render from '../nav/render.js';
import trackShow from './trackShow.js';
import autoSpeak from './autoSpeak.js';
import stopSlow from './stopSlow.js';
/** Операция над сессией → перерисовать с анимацией (card-in вперёд / card-back назад) → новая карточка: отсчёт поведения с нуля. */
export default async function studyStep(op, anim = 'card-in') {
  stopSlow();
  await op(state.session);
  state.revealed = false;
  state.quiz = null;
  state.speech = null;
  state.anim = anim;
  await render();
  trackShow(state.session, anim === 'card-back');
  await autoSpeak('show');
}
