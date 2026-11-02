import { state } from '../state/appState.js';
import { PREF_TITLES } from '../constants/PREF_TITLES.js';
import prefTile from './prefTile.js';
import prefLabel from '../settings/prefLabel.js';
/** Все настройки внешнего вида, учёбы и озвучки: плитки по два в ряд. */
export default function settingTiles() {
  const tiles = Object.entries(PREF_TITLES).map(([key, title]) => prefTile({ act: 'cyclePref', title, value: prefLabel(key, state.prefs[key]), data: { key } }));
  return `<div class="tiles">${tiles.join('')}</div>`;
}
