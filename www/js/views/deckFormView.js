import { state } from '../state/appState.js';
import getDeckWithProject from '../repo/getDeckWithProject.js';
import listProjects from '../repo/listProjects.js';
import header from '../components/header.js';
import backButton from '../components/backButton.js';
import labeledField from '../components/labeledField.js';
import selectField from '../components/selectField.js';
import formButtons from '../components/formButtons.js';
export default async function deckFormView() {
  const id = state.nav.id;
  const [deck, projects] = await Promise.all([id ? getDeckWithProject(id) : null, listProjects()]);
  const projectId = deck ? deck.language_id : state.nav.projectId;
  return header(id ? 'Колода' : 'Новая колода', { left: backButton('back') }) +
    labeledField({ id: 'name', label: 'Название', value: deck?.name ?? '', placeholder: 'Например, Договоры' }) +
    selectField({ id: 'project', label: 'Проект', options: projects.map(c => [c.id, c.name]), value: projectId }) +
    formButtons({ save: 'saveDeck', del: id ? 'delDeck' : '' });
}
