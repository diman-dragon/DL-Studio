/** Родительский экран (запасной путь «Назад», если истории браузера нет). */
export default function parentOf(nav) {
  if (nav.v === 'projectForm') return nav.id ? { view: 'project', params: { id: nav.id } } : { view: 'library', params: {} };
  if (nav.v === 'deckForm') return nav.id ? { view: 'deck', params: { id: nav.id } } : { view: 'project', params: { id: nav.projectId } };
  if (nav.v === 'cardForm') return { view: 'deck', params: { id: nav.deckId } };
  if (nav.v === 'session') return { view: 'study', params: {} };
  return { view: 'library', params: {} };
}
