import { state } from '../state/appState.js';
import currentCard from './currentCard.js';
import cardDir from './cardDir.js';
import speakText from '../tts/speakText.js';
/** Автоозвучка изучаемой стороны по настройке. */
export default async function autoSpeak(trigger) {
  const { session, prefs } = state;
  const card = session && currentCard(session);
  if (!card) return;
  if (prefs.autoplay !== 'on') return;
  if (cardDir(session, card) === 'fwd' ? trigger === 'show' : trigger === 'reveal') await speakText(card.front, card.tts);
}
