import { state } from '../state/appState.js';
import { CARD_FIELDS } from '../constants/CARD_FIELDS.js';
import getCard from '../repo/getCard.js';
import listAllDecks from '../repo/listAllDecks.js';
import header from '../components/header.js';
import backButton from '../components/backButton.js';
import labeledField from '../components/labeledField.js';
import selectField from '../components/selectField.js';
import formButtons from '../components/formButtons.js';
export default async function cardFormView() {
  const id = state.nav.id;
  const [card, decks] = await Promise.all([id ? getCard(id) : null, listAllDecks()]);
  const fields = CARD_FIELDS.map(f => labeledField({ id: f.id, label: f.label, value: card?.[f.col] ?? '', placeholder: f.ph, multiline: f.multiline })).join('');
  return header(id ? 'Карточка' : 'Новая карточка', { left: backButton('back') }) + fields +
    selectField({ id: 'deck', label: 'Колода', options: decks.map(d => [d.id, `${d.pname} · ${d.name}`]), value: card ? card.deck_id : state.nav.deckId }) +
    formButtons({ save: 'saveCard', more: !id, del: id ? 'delCard' : '' });
}
