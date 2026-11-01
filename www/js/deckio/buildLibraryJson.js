import { LIBRARY_FORMAT } from '../constants/LIBRARY_FORMAT.js';
import { DECK_VERSION } from '../constants/DECK_VERSION.js';
import listProjects from '../repo/listProjects.js';
import listDecksOfProject from '../repo/listDecksOfProject.js';
import listCards from '../repo/listCards.js';
const cardJson = c => ({
  front: c.front, back: c.back,
  ...(c.example && { example: c.example }), ...(c.example_tr && { example_translation: c.example_tr })
});
export default async function buildLibraryJson() {
  const projects = [];
  for (const p of await listProjects()) {
    const decks = [];
    for (const d of await listDecksOfProject(p.id)) decks.push({ name: d.name, cards: (await listCards(d.id)).map(cardJson) });
    projects.push({ name: p.name, tts: p.tts, decks });
  }
  return { format: LIBRARY_FORMAT, version: DECK_VERSION, projects };
}
