import listProjects from '../repo/listProjects.js';
import header from '../components/header.js';
import button from '../components/button.js';
import entry from '../components/entry.js';
import pagedList from '../components/pagedList.js';
import layoutToggle from '../components/layoutToggle.js';
import emptyState from '../components/emptyState.js';
import row from '../components/row.js';
import avatar from '../components/avatar.js';
import projectStudyStats from '../components/projectStudyStats.js';
import projectStudyMeta from '../components/projectStudyMeta.js';
/** Главный экран — только проекты. Нажатие на проект сразу запускает интервальное повторение по его карточкам. */
export default async function studyHubView() {
  const projects = await listProjects();
  const top = header('Учёба', { right: layoutToggle() });
  if (!projects.length) return top + emptyState({ icon: '🧠', title: 'Начните с проекта', text: 'Создайте проект и добавьте карточки — здесь появится очередь повторений.', actions: row(button({ act: 'goLibrary', cls: 'p', label: 'Открыть библиотеку' })) });
  const items = projects.map(p => {
    return entry({ act: 'startStudy', id: `p:${p.id}`, title: p.name,
      sub: projectStudyMeta(p), lead: avatar(p.name), mark: projectStudyStats(p) });
  });
  return top + pagedList(items);
}
