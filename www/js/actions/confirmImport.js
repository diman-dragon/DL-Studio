import { state } from '../state/appState.js';
import readField from '../dom/readField.js';
import toast from '../dom/toast.js';
import findOrCreateProject from '../deckio/findOrCreateProject.js';
import findOrCreateDeck from '../deckio/findOrCreateDeck.js';
import addMissingCards from '../deckio/addMissingCards.js';
import guessTts from '../deckio/guessTts.js';
import go from '../nav/go.js';
/** «Сохранить» на экране «Импорт»: карточки — в выбранный проект (или в новый «Импорт») и колоду; дубли не создаются. */
export default async function confirmImport() {
  const d = state.importDraft;
  if (!d) return;
  const projectId = readField('project') ? +readField('project') : await findOrCreateProject('Импорт', d.lang || guessTts(d.cards));
  const deckId = await findOrCreateDeck(projectId, readField('name') || d.name, 'cards');
  const added = await addMissingCards(deckId, d.cards, kind);
  state.importDraft = null;
  toast(`Добавлено карточек: ${added} из ${d.cards.length}`);
  state.nav = { v: 'library' };
  return go('deck', { id: deckId });
}
