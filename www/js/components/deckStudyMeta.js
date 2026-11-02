import { state } from '../state/appState.js';
import { DECK_PREFS } from '../constants/DECK_PREFS.js';
import deckPrefOf from '../settings/deckPrefOf.js';
const label = (name, value) => DECK_PREFS[name].find(([v]) => v === value)[1];
/** Две короткие строки под названием колоды: карточек и к повторению; её собственные параметры (новых в день · в сессии · интервалы). */
export default function deckStudyMeta(deck, prefs) {
  const own = name => deckPrefOf(prefs, deck.id, name), pick = name => (own(name) === 'auto' ? state.prefs[name] : own(name));
  const max = pick('sessionMax') === '9999' ? 'все' : pick('sessionMax');
  return `${deck.n} карт. · ${deck.due} повторить\n${pick('newPerDay')}/день · ${max} в сессии · ${label('pace', own('pace')).toLowerCase()}`;
}
