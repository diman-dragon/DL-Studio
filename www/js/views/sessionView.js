import { state } from '../state/appState.js';
import { MODE_SHORT } from '../constants/MODE_SHORT.js';
import currentCard from '../study/currentCard.js';
import cardDir from '../study/cardDir.js';
import makeQuiz from '../study/makeQuiz.js';
import progressOf from '../study/progressOf.js';
import header from '../components/header.js';
import backButton from '../components/backButton.js';
import button from '../components/button.js';
import progressBar from '../components/progressBar.js';
import studyFace from './studyFace.js';
import listenFace from './listenFace.js';
import speakFace from './speakFace.js';
import studyControls from './studyControls.js';
import quizControls from './quizControls.js';
import studyFinishedView from './studyFinishedView.js';
const FACES = {
  srs: (c, f, dir, q) => studyFace(c, f, dir, q),
  cards: (c, f, dir) => studyFace(c, f, dir),
  listen: (c, f, dir, q) => listenFace(c, f, q),
  speak: (c, f, dir, q, speech) => speakFace(c, f, speech)
};
/** Экран учёбы: всё помещается без прокрутки. В шапке: ↶ — отменить последнюю оценку, направление (только в повторении). */
export default async function sessionView() {
  const s = state.session, card = currentCard(s);
  const undo = s.undo ? button({ act: 'undo', cls: 'sm', label: '↶', aria: 'Отменить последнюю оценку' }) : '';
  const head = header(MODE_SHORT[s.mode], { left: backButton('back'), right: undo });
  if (!card) return head + studyFinishedView(s);
  const dir = cardDir(s, card);
  if (state.quiz?.id !== card.id) state.quiz = s.mode === 'speak' ? null : makeQuiz(s, card, dir, state.prefs.check);
  const q = state.quiz, flipped = state.revealed || !!q?.result;
  const face = q?.kind === 'speak' ? speakFace(card, flipped, state.speech?.id === card.id ? state.speech : null) : FACES[s.mode](card, flipped, dir, q, state.speech?.id === card.id ? state.speech : null);
  return head + progressBar(progressOf(s), `${card.pname} · ${card.dname}`) + face + `<div id="controls">${q?.kind === 'speak' ? studyControls() : q ? quizControls(q) : studyControls()}</div>`;
}
