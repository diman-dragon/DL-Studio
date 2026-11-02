import { DECK_DEFAULTS } from '../constants/DECK_DEFAULTS.js';
/** Значение параметра колоды из loadDeckPrefs (нет своего — 'auto' / обычный темп). */
export default function deckPrefOf(prefs, deckId, name) { return prefs[deckId]?.[name] ?? DECK_DEFAULTS[name]; }
