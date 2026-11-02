/** Свои параметры колод (settings/loadDeckPrefs) → числа для сессии: { id: { perDay?, max?, pace } }; нет своего значения — поле не задано (действует общее). */
export default function deckLimits(prefs) {
  const num = v => (v === undefined || v === 'auto' ? undefined : Number(v));
  return Object.fromEntries(Object.entries(prefs).map(([id, p]) => [id, { perDay: num(p.newPerDay), max: num(p.sessionMax), inOrder: p.newOrder === 'order', pace: Number(p.pace ?? 1) }]));
}
