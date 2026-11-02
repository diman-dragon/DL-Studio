import { state } from '../state/appState.js';
import currentCard from '../study/currentCard.js';
import cardDir from '../study/cardDir.js';
import taskFor from '../study/taskFor.js';
import progressOf from '../study/progressOf.js';
import header from '../components/header.js';
import backButton from '../components/backButton.js';
import button from '../components/button.js';
import progressBar from '../components/progressBar.js';
import studyFace from '../components/studyFace.js';
import speakFace from '../components/speakFace.js';
import studyControls from '../components/studyControls.js';
import quizControls from '../components/quizControls.js';
import studyFinishedView from '../components/studyFinished.js';

/** Экран единственного режима — интервального повторения. */
export default async function sessionView() {
  const s = state.session, card = currentCard(s);
  const undo = s.undo ? button({ act: 'undo', cls: 'sm', label: '↶', aria: 'Отменить последнюю оценку' }) : '';
  const head = header('Повторение', { left: backButton('back'), right: undo });
  if (!card) return head + studyFinishedView(s);
  const dir = cardDir(s, card);
  if (state.quiz?.id !== card.id) state.quiz = taskFor(s, card, dir);
  const q = state.quiz, flipped = state.revealed || !!q?.result;
  const face = q?.kind === 'speak'
    ? speakFace(card, flipped, state.speech?.id === card.id ? state.speech : null, q)
    : studyFace(card, flipped, dir, q);
  return head + progressBar(progressOf(s), `${card.pname} · ${card.dname}`) + face + `<div id="controls">${q?.kind === 'speak' ? studyControls() : q ? quizControls(q) : studyControls()}</div>`;
}
