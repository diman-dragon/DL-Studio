import { appEl } from '../dom/appEl.js';
import { state } from '../state/appState.js';
import { TAB_OF } from '../constants/TAB_OF.js';
import views from '../views/index.js';
import tabBar from '../components/tabBar.js';
import layoutPager from '../pager/layoutPager.js';
import fitFaces from '../dom/fitFaces.js';
import setAwake from '../native/setAwake.js';
import { slow } from '../state/slowState.js';
/** Экран + нижняя панель (всегда на месте) + постраничный вывод. Анимация проигрывается один раз. Во время учёбы экран не гаснет. */
export default async function render() {
  state.pager = { items: [], titles: [], fixed: false };
  const body = await views[state.nav.v]();
  const form = state.nav.v.endsWith('Form') ? ' form' : '';
  appEl.innerHTML = `<div class="view ${state.anim}${form}">${body}</div>${tabBar(TAB_OF[state.nav.v] ?? '')}`;
  state.anim = '';
  layoutPager();
  fitFaces();
  const awake = state.nav.v === 'session';
  if (!awake) slow.run++;   // ушли с карточки — замедленный повтор по словам прекращается
  if (awake !== state.awake) { state.awake = awake; setAwake(awake); }
}
