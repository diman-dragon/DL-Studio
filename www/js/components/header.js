import { state } from '../state/appState.js';
import { HELP } from '../constants/HELP.js';
import esc from '../util/esc.js';
import infoButton from './infoButton.js';
/** Шапка: слева «назад»/знак, справа действия экрана и значок справки ⓘ (если для экрана она есть). Длинное название мельче (long > 28 знаков, xlong > 60) и переносится на несколько строк. */
export default function header(title, { left = '', right = '' } = {}) {
  const size = title.length > 60 ? ' class="xlong"' : title.length > 28 ? ' class="long"' : '';
  return `<header>${left}<h1${size}>${esc(title)}</h1>${right}${HELP[state.nav.v] ? infoButton() : ''}</header>`;
}
