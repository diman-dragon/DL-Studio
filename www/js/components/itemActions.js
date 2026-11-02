import iconButton from './iconButton.js';
import penIcon from './penIcon.js';
import trashIcon from './trashIcon.js';
/** Карандаш и корзина на элементе списка: edit / del — имена действий, id — что менять / удалять. Готовый HTML. */
export default function itemActions(edit, del, id) {
  return `<span class="acts">${iconButton(edit, 'Изменить', penIcon(), id)}${iconButton(del, 'Удалить', trashIcon(), id)}</span>`;
}
