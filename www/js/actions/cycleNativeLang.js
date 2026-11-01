import { state } from '../state/appState.js';
import savePref from '../settings/savePref.js';
import render from '../nav/render.js';

const LANGS = ['system', 'ru', 'sr', 'en'];
export default async function cycleNativeLang() {
  const current = state.prefs.nativeLang || 'system';
  const next = LANGS[(LANGS.indexOf(current) + 1) % LANGS.length];
  await savePref('nativeLang', next);
  return render();
}
