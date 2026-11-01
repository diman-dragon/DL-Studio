import trackPause from '../study/trackPause.js';
import trackResume from '../study/trackResume.js';
import syncIcon from '../study/syncIcon.js';
import { state } from '../state/appState.js';
import render from '../nav/render.js';
/** Свернули приложение — время на карточке стоит; вернулись — идёт дальше и пересчитывается значок. */
export default function onVisibility() {
  if (document.hidden) return trackPause();
  syncIcon();
  if (state.nav.v === 'settings') render();
  return trackResume();
}
