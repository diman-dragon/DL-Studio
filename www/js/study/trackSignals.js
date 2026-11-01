import { state } from '../state/appState.js';
import { track } from '../state/trackState.js';
import trackAdd from './trackAdd.js';
/** Итог по карточке, которую покидают: { think, look, dwell, flips, revisit, away, correct, hinted, plays, kind, listen, speak, score, attempts } (kind — вид вопроса, null — переворот; score / attempts — произношение). */
export default function trackSignals() {
  trackAdd();
  const { think, front, back, flips, revisit, away, correct, hinted, plays } = track;
  return { think, look: back, dwell: front + back, flips, revisit, away, correct, hinted, plays, kind: state.quiz?.kind ?? null, listen: state.session?.mode === 'listen', speak: state.session?.mode === 'speak' || state.quiz?.kind === 'speak', score: state.speech?.score ?? null, attempts: state.speech?.attempts ?? 0 };
}
