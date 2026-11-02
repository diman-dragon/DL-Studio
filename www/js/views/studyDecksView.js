import { state } from '../state/appState.js';
import getProject from '../repo/getProject.js';
import listDecksOfProject from '../repo/listDecksOfProject.js';
import loadDeckPrefs from '../settings/loadDeckPrefs.js';
import header from '../components/header.js';
import backButton from '../components/backButton.js';
import entry from '../components/entry.js';
import pagedList from '../components/pagedList.js';
import layoutToggle from '../components/layoutToggle.js';
import emptyState from '../components/emptyState.js';
import avatar from '../components/avatar.js';
import studyMark from '../components/studyMark.js';
import deckStudyMeta from '../components/deckStudyMeta.js';
const sum = (decks, key) => decks.reduce((n, d) => n + Number(d[key]), 0);
/** Колоды проекта для учёбы: «Весь проект» и каждая колода со своим повторением; нажатие запускает сессию. Правок на «Учёбе» нет: параметры колоды меняются только в библиотеке. */
export default async function studyDecksView() {
  const id = state.nav.id;
  const [project, decks, prefs] = await Promise.all([getProject(id), listDecksOfProject(id), loadDeckPrefs()]);
  const top = header(project.name, { left: backButton('goStudy'), right: layoutToggle() });
  if (!decks.length) return top + emptyState({ icon: '🗂️', title: 'Колод нет', text: 'Создайте колоду и добавьте карточки в библиотеке.' });
  const all = entry({ act: 'startStudy', id: `p:${id}`, title: 'Весь проект', lead: avatar('∑'), mark: studyMark({ due: sum(decks, 'due'), fresh: sum(decks, 'fresh'), total: sum(decks, 'n') }),
    sub: `${sum(decks, 'n')} карт. · ${sum(decks, 'due')} повторить\nкаждая колода по своим правилам` });
  const items = decks.map(d => entry({ act: 'startStudy', id: `d:${d.id}`, title: d.name, sub: deckStudyMeta(d, prefs), avatarText: d.name,
    mark: studyMark({ due: d.due, fresh: d.fresh, total: d.n }) }));
  return top + pagedList([all, ...items]);
}
