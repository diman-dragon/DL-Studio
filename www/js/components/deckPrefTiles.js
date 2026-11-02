import { DECK_PREFS } from '../constants/DECK_PREFS.js';
import { DECK_TITLES } from '../constants/DECK_TITLES.js';
import prefTile from './prefTile.js';
import deckPrefOf from '../settings/deckPrefOf.js';
/** Свои параметры повторения колоды (prefs — settings/loadDeckPrefs): новых в день, карточек за сессию, темп интервалов. */
export default function deckPrefTiles(deckId, prefs) {
  const tiles = Object.keys(DECK_PREFS).map(name => {
    const value = DECK_PREFS[name].find(([v]) => v === deckPrefOf(prefs, deckId, name))[1];
    return prefTile({ act: 'cycleDeckPref', title: DECK_TITLES[name], value, data: { id: deckId, key: name } });
  });
  return `<h3>Повторение колоды</h3><div class="tiles">${tiles.join('')}</div>`;
}
