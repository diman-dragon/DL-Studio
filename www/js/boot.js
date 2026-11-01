import { appEl } from './dom/appEl.js';
import loadPrefs from './settings/loadPrefs.js';
import applyPrefs from './settings/applyPrefs.js';
import watchSystemTheme from './events/watchSystemTheme.js';
import bindEvents from './events/bindEvents.js';
import render from './nav/render.js';
import syncIcon from './study/syncIcon.js';
export default async function boot() {
  try {
    await loadPrefs();
    applyPrefs();
    watchSystemTheme();
    bindEvents();
    await render();
    syncIcon();
  } catch (e) { appEl.textContent = 'Ошибка запуска: ' + (e.message || e); }
}
