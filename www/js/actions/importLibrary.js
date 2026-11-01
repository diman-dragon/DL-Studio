import { state } from '../state/appState.js';
import safOpen from '../native/safOpen.js';
import readImportFile from '../deckio/readImportFile.js';
import saveLibrary from '../deckio/saveLibrary.js';
import toast from '../dom/toast.js';
import go from '../nav/go.js';
/** Импорт файла любого поддерживаемого формата: наш JSON — сразу; остальное (CSV, Anki, Excel …) — на экран «Импорт» выбрать колоду. */
export default async function importLibrary() {
  const r = await readImportFile(await safOpen());
  if (r.kind === 'library') {
    const s = await saveLibrary(r.library);
    toast(`Импорт: проектов ${s.projects}, колод ${s.decks}, новых карточек ${s.added}`);
    return go('library');
  }
  state.importDraft = { name: r.name, format: r.format, cards: r.cards };
  return go('importForm');
}
