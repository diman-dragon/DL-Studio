import { HELP } from '../constants/HELP.js';
import esc from '../util/esc.js';
import sheetShell from './sheetShell.js';
/** Нижнее окно справки по экрану view; пусто, если справки для него нет. */
export default function helpSheet(view) {
  const lines = HELP[view];
  return lines ? sheetShell('Как пользоваться', `<ul class="help">${lines.map(t => `<li>${esc(t)}</li>`).join('')}</ul>`, 'Понятно') : '';
}
