import { state } from '../state/appState.js';
import getDeckWithProject from '../repo/getDeckWithProject.js';
import listProjects from '../repo/listProjects.js';
import loadDeckPrefs from '../settings/loadDeckPrefs.js';
import header from '../components/header.js';
import backButton from '../components/backButton.js';
import labeledField from '../components/labeledField.js';
import selectField from '../components/selectField.js';
import deckPrefTiles from '../components/deckPrefTiles.js';
import formButtons from '../components/formButtons.js';
/** Колода: название, проект и (у существующей) её собственные параметры повторения — плитки-переключатели сохраняются сразу. */
export default async function deckFormView() {
  const id = state.nav.id;
  const [deck, projects, prefs] = await Promise.all([id ? getDeckWithProject(id) : null, listProjects(), loadDeckPrefs()]);
  const projectId = deck ? deck.language_id : state.nav.projectId;
  return header(id ? 'Колода' : 'Новая колода', { left: backButton('back') }) +
    labeledField({ id: 'name', label: 'Название', value: deck?.name ?? '', placeholder: 'Например, Договоры' }) +
    selectField({ id: 'project', label: 'Проект', options: projects.map(c => [c.id, c.name]), value: projectId }) +
    (id ? deckPrefTiles(id, prefs) : '') +
    formButtons({ save: 'saveDeck', del: id ? 'delDeck' : '' });
}
