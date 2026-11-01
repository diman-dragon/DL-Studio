import { state } from '../state/appState.js';
import ttsStop from '../native/ttsStop.js';
import popBack from './popBack.js';
import render from './render.js';
/** Системная «Назад»: сначала закрывает меню выбора колоды; иначе ровно один шаг назад; на первом экране (библиотека) остаёмся. */
export default async function systemBack() {
  await ttsStop().catch(() => {});
  if (state.sheet) { state.sheet = null; return render(); }
  if (await popBack() || state.nav.v === 'study') return;
  state.nav = { v: 'study' };
  state.anim = 'view-in';
  state.page = 0;
  await render();
}
