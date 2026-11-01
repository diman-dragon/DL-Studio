import findDeckByName from '../repo/findDeckByName.js';
import insertDeck from '../repo/insertDeck.js';
/** Колода проекта по имени (если нет — создаётся обычная карточная колода). */
export default async function findOrCreateDeck(projectId, name, kind = 'cards') {
  return (await findDeckByName(projectId, name))?.id ?? insertDeck(projectId, name, kind);
}
