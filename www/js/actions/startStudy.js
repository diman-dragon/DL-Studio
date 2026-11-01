import { state } from '../state/appState.js';
import openStudy from '../study/openStudy.js';
/** Из экрана колод режима: mode — режим ('blitz' / 'quest' — игры, 'dialog' — сцена диалога), scope — 'all' | 'd:<id>'. */
export default function startStudy(d) {
  state.sheet = null;
  return openStudy('srs', d.scope || d.id);
}
