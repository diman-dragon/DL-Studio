import esc from '../util/esc.js';
import button from './button.js';
/** Плитка-переключатель: нажал — значение сменилось и так по кругу. data — атрибуты действия (key, id…); value — подпись текущего значения. */
export default function prefTile({ act, title, value, data = {}, hint = '' }) {
  return button({ act, cls: 'tile', data, aria: `${title}: ${value}. Нажмите, чтобы переключить${hint ? `. ${hint}` : ''}`,
    label: `<span class="tt">${esc(title)}</span><b class="tv">${esc(value)}</b>` });
}
