import { DECK_PREFS } from '../constants/DECK_PREFS.js';
import loadDeckPrefs from '../settings/loadDeckPrefs.js';
import saveDeckPref from '../settings/saveDeckPref.js';
import deckPrefOf from '../settings/deckPrefOf.js';
import nextOption from '../util/nextOption.js';
/** Плитка параметра колоды (data-id колоды, data-key параметр): нажатие — следующее значение по кругу; сохраняется сразу, подпись меняется на месте. */
export default async function cycleDeckPref(d, el) {
  const options = DECK_PREFS[d.key], value = nextOption(options, deckPrefOf(await loadDeckPrefs(), d.id, d.key));
  await saveDeckPref(d.id, d.key, value);
  el.querySelector('.tv').textContent = options.find(([v]) => v === value)[1];
}
