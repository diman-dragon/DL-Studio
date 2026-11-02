import { state } from '../state/appState.js';
import { PREF_OPTIONS } from '../constants/PREF_OPTIONS.js';
import savePref from '../settings/savePref.js';
import prefLabel from '../settings/prefLabel.js';
import nextOption from '../util/nextOption.js';
/** Плитка настройки: нажатие — следующее значение по кругу (data-key); подпись меняется на месте, без перерисовки экрана. */
export default async function cyclePref(d, el) {
  const value = nextOption(PREF_OPTIONS[d.key], state.prefs[d.key]);
  await savePref(d.key, value);
  state.quiz = null;
  el.querySelector('.tv').textContent = prefLabel(d.key, value);
}
