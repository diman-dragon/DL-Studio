import { state } from '../state/appState.js';
import startSession from './startSession.js';
import loadLimits from './loadLimits.js';
import checkSpeech from './checkSpeech.js';
import trackShow from './trackShow.js';
import autoSpeak from './autoSpeak.js';
import go from '../nav/go.js';
/** Запуск интервального повторения по области scope ('all' | 'p:<id>' | 'd:<id>'). */
export default async function openStudy(mode, scope) {
  const session = await startSession(scope, mode, await loadLimits());
  if (!session.map.size) throw new Error('В колоде нет карточек');
  await checkSpeech(session);
  state.session = session;
  state.quiz = null;
  state.speech = null;
  await go('session');
  trackShow(session);
  await autoSpeak('show');
}
