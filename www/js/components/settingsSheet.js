import { APP_VERSION } from '../constants/APP_VERSION.js';
import { APP_NAME } from '../constants/APP_NAME.js';
import sheetShell from './sheetShell.js';
import jsonFormat from './jsonFormat.js';
import aboutBlock from './aboutBlock.js';
import logoMark from './logoMark.js';
/** Нижнее окно настроек: формат JSON или «О приложении» (по state.sheet); пусто, если окно не открыто. */
export default function settingsSheet(sheet) {
  if (sheet === 'json') {
    return sheetShell('Формат JSON', jsonFormat() + '<p class="note muted">front — изучаемый язык; back — перевод на нативный язык; example, example_translation и cloze — необязательные поля.</p>', 'Закрыть');
  }
  return sheet === 'about' ? sheetShell(APP_NAME, aboutBlock({ name: 'DL-Studio', version: APP_VERSION, logo: logoMark() }), 'Закрыть') : '';
}
