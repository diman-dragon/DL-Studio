/** Новые карточки: у колод с порядком «По порядку» (limits.decks[id].inOrder) они идут в том порядке, как добавлены (ord), на тех же местах, что заняла колода среди перемешанных. */
export default function orderFresh(fresh, limits) {
  const out = [...fresh];
  for (const [id, own] of Object.entries(limits.decks ?? {})) {
    if (!own.inOrder) continue;
    const at = fresh.flatMap((c, i) => (String(c.deck_id) === id ? [i] : []));
    const sorted = at.map(i => fresh[i]).sort((a, b) => a.ord - b.ord || a.id - b.id);
    at.forEach((i, k) => { out[i] = sorted[k]; });
  }
  return out;
}
