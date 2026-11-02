import { state } from '../state/appState.js';
import countNewByDeck from '../repo/countNewByDeck.js';
import loadDeckPrefs from '../settings/loadDeckPrefs.js';
import deckLimits from './deckLimits.js';
import dayStart from '../util/dayStart.js';
/**
 * Лимиты из настроек + сколько новых уже начато сегодня по каждой колоде (читаем из базы): perDay, used, max
 * и decks — свои параметры колод (новых в день, карточек в плане, темп интервалов); у колоды без своих действуют общие.
 */
export default async function loadLimits() {
  const [used, prefs] = await Promise.all([countNewByDeck(dayStart()), loadDeckPrefs()]);
  return { perDay: Number(state.prefs.newPerDay), used, max: Number(state.prefs.sessionMax), decks: deckLimits(prefs) };
}
