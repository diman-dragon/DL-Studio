import { state } from '../state/appState.js';
import esc from '../util/esc.js';
import listProjects from '../repo/listProjects.js';
import header from '../components/header.js';
import backButton from '../components/backButton.js';
import button from '../components/button.js';
import row from '../components/row.js';
import labeledField from '../components/labeledField.js';
import selectField from '../components/selectField.js';
import formButtons from '../components/formButtons.js';
/** «Импорт» не нашего JSON: пока ничего не разобрано — вставка текста или выбор файла; потом — сводка, проект и колода. */
export default async function importFormView() {
  const d = state.importDraft, head = header('Импорт', { left: backButton('back') });
  if (!d) {
    return head + '<label class="field"><span>Вставьте список («слово — перевод»), CSV, TSV или экспорт Anki / Quizlet</span>' +
      '<textarea id="paste" rows="9" placeholder="apple — яблоко&#10;dog — собака"></textarea></label>' +
      row(button({ act: 'parsePaste', cls: 'p', label: 'Разобрать' }) + button({ act: 'importLibrary', label: 'Файл…' }) + button({ act: 'back', label: 'Отмена' }), 'foot');
  }
  const projects = await listProjects();
  const sample = d.cards.slice(0, 3).map(c => `${esc(c.front)} — ${esc(c.back)}`).join('<br>');
  return head + `<div class="muted note">Формат: ${esc(d.format)}<br>Найдено карточек: <b>${d.cards.length}</b></div><div class="preview">${sample}</div>` +
    selectField({ id: 'project', label: 'Проект', options: [['', '➕ Новый проект «Импорт»'], ...projects.map(p => [p.id, p.name])], value: projects[0]?.id ?? '' }) +
    labeledField({ id: 'name', label: 'Колода', value: d.name }) + formButtons({ save: 'confirmImport' });
}
