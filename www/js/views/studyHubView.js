import listProjects from '../repo/listProjects.js';
import header from '../components/header.js';
import button from '../components/button.js';
import entry from '../components/entry.js';
import pagedList from '../components/pagedList.js';
import layoutToggle from '../components/layoutToggle.js';
import emptyState from '../components/emptyState.js';
import row from '../components/row.js';
import avatar from '../components/avatar.js';
/** Главный экран — только проекты. Нажатие на проект сразу запускает интервальное повторение по его карточкам. */
export default async function studyHubView() {
  const projects = await listProjects();
  const top = header('Учёба', { right: layoutToggle() });
  if (!projects.length) return top + emptyState({ icon: '🧠', title: 'Начните с проекта', text: 'Создайте проект и добавьте карточки — здесь появится очередь повторений.', actions: row(button({ act: 'goLibrary', cls: 'p', label: 'Открыть библиотеку' })) });
  const items = projects.map(p => {
    const overdue = Number(p.overdue || 0), old = Number(p.late || 0), lateClass = overdue ? 'late' : '';
    const warning = overdue ? `<span class="overdue ${old ? 'overdue-hot' : ''}">${old ? '⚠ ' : ''}${overdue} просрочено</span>` : '<span class="overdue ok">нет просрочки</span>';
    const sub = `${p.cards} карточек · ${p.due} к повторению · ${overdue} просрочено`;
    return entry({ act: 'startStudy', id: `p:${p.id}`, title: p.name, sub, lead: avatar(p.name), mark: `<span class="project-stats ${lateClass}">${warning}</span>` });
  });
  return top + pagedList(items);
}
