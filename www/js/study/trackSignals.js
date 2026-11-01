import { state } from '../state/appState.js';
import { track } from '../state/trackState.js';
import trackAdd from './trackAdd.js';
/** Итог по карточке, которую покидают: поведение, ответ на задание и результат произношения. */
export default function trackSignals() {
  trackAdd();
  const { think, front, back, flips, revisit, away, correct, hinted, plays } = track;
  return { think, look: back, dwell: front + back, flips, revisit, away, correct, hinted, plays, kind: state.quiz?.kind ?? null, speak: state.quiz?.kind === 'speak', score: state.speech?.score ?? null, attempts: state.speech?.attempts ?? 0 };
}
