import { state } from '../state/appState.js';
import currentCard from './currentCard.js';
import cardDir from './cardDir.js';
import speakText from '../tts/speakText.js';
import spokenText from './spokenText.js';
/** Автоозвучка изучаемой стороны по настройке. */
export default async function autoSpeak(trigger) {
  const { session, prefs } = state;
  const card = session && currentCard(session);
  if (!card) return;
  const quiz = state.quiz;
  if (quiz?.kind?.startsWith('audio-')) {
    if (trigger === 'show') await speakText(spokenText(card), card.tts);
    return;
  }
  if (quiz?.kind === 'speak') {
    if (trigger === 'show') await speakText(quiz.answer || card.front, card.tts);
    return;
  }
  if (prefs.autoplay !== 'on') return;
  if (cardDir(session, card) === 'fwd' ? trigger === 'show' : trigger === 'reveal') await speakText(card.front, card.tts);
}
