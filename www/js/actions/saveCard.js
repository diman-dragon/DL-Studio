import { state } from '../state/appState.js';
import { CARD_FIELDS } from '../constants/CARD_FIELDS.js';
import readField from '../dom/readField.js';
import toast from '../dom/toast.js';
import getCard from '../repo/getCard.js';
import updateCard from '../repo/updateCard.js';
import moveCard from '../repo/moveCard.js';
import insertCards from '../repo/insertCards.js';
import nextCardOrd from '../repo/nextCardOrd.js';
import refreshCard from '../study/refreshCard.js';
import goBack from '../nav/goBack.js';
import go from '../nav/go.js';
/** data-more — сохранить и открыть пустую форму для следующей карточки. */
export default async function saveCard(d) {
  const id = state.nav.id, deckId = +readField('deck');
  const card = Object.fromEntries(CARD_FIELDS.map(f => [f.key, readField(f.id)]));
  if (!card.front || !card.back) return toast('Заполните слово и перевод');
  const old = id ? await getCard(id) : null;
  if (id) {
    await updateCard(id, card);
    refreshCard(state.session, id, card);
    if (old.deck_id !== deckId) await moveCard(id, deckId, await nextCardOrd(deckId));
  } else await insertCards(deckId, [card], await nextCardOrd(deckId));
  toast('Сохранено');
  return d.more ? go('cardForm', { deckId }) : goBack();
}
