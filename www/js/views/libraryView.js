import listProjects from '../repo/listProjects.js';
import header from '../components/header.js';
import button from '../components/button.js';
import entry from '../components/entry.js';
import pagedList from '../components/pagedList.js';
import layoutToggle from '../components/layoutToggle.js';
import itemActions from '../components/itemActions.js';
import studyMark from '../components/studyMark.js';
import emptyState from '../components/emptyState.js';
import row from '../components/row.js';
import projectLibraryMeta from '../components/projectLibraryMeta.js';
/** Верхний уровень библиотеки: проекты (метка учёбы, ✎ изменить, 🗑 удалить). Импорт и экспорт — в настройках («Данные»). */
export default async function libraryView() {
  const projects = await listProjects();
  const items = projects.map(c => entry({
    act: 'goProject', id: c.id, title: c.name, sub: projectLibraryMeta({ decks: c.n, cards: c.cards }),
    mark: studyMark({ due: c.due, fresh: c.fresh, total: c.cards }), acts: itemActions('editProject', 'delProject', c.id), avatarText: c.name
  }));
  const body = items.length ? pagedList(items)
    : emptyState({ icon: '🌍', title: 'Проектов пока нет', text: 'Проект — это язык, тема или что угодно: «Английский», «Даты», «Юр. термины». Готовую библиотеку можно импортировать в настройках (⚙️ → Данные).' });
  return header('Библиотека', { right: layoutToggle() }) + body + row(button({ act: 'newProject', cls: 'p', label: '+ Проект' }), 'foot');
}
