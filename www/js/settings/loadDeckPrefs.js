import { DECK_PREFS } from '../constants/DECK_PREFS.js';
import listSettings from '../repo/listSettings.js';
/** Свои параметры повторения колод из settings: { id колоды: { newPerDay, sessionMax, pace } }; неизвестные ключи и значения игнорируются. */
export default async function loadDeckPrefs() {
  const out = {};
  for (const { key, value } of await listSettings()) {
    const [tag, id, name] = key.split(':');
    if (tag === 'deck' && DECK_PREFS[name]?.some(([v]) => v === value)) (out[id] ??= {})[name] = value;
  }
  return out;
}
