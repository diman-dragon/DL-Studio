import { state } from '../state/appState.js';
import readImportText from '../deckio/readImportText.js';
import saveLibrary from '../deckio/saveLibrary.js';
import toast from '../dom/toast.js';
import render from '../nav/render.js';
import go from '../nav/go.js';
/** «Разобрать» вставленный текст: найденные карточки — на выбор колоды; наш JSON импортируется сразу. */
export default async function parsePaste() {
  const text = document.getElementById('paste').value;
  if (!text.trim()) return toast('Вставьте текст');
  const r = readImportText('Вставленный текст', text);
  if (r.kind === 'library') {
    const s = await saveLibrary(r.library);
    toast(`Импорт: проектов ${s.projects}, колод ${s.decks}, новых карточек ${s.added}`);
    return go('library');
  }
  state.importDraft = { name: r.name, format: r.format, cards: r.cards };
  return render();
}
