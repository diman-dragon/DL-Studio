/**
 * План дня: просроченные (давние первыми), затем новые. У каждой колоды свои пределы: новых в день (limits.decks[id].perDay, иначе общий perDay
 * за вычетом уже начатых сегодня — limits.used) и карточек в плане (decks[id].max); весь план не больше limits.max.
 */
export default function takePlan(due, fresh, limits) {
  const plan = [], taken = {}, newLeft = {}, own = id => limits.decks?.[id] ?? {};
  const room = c => plan.length < limits.max && (taken[c.deck_id] ?? 0) < (own(c.deck_id).max ?? Infinity);
  const add = c => { plan.push(c); taken[c.deck_id] = (taken[c.deck_id] ?? 0) + 1; };
  for (const c of due) if (room(c)) add(c);
  for (const c of fresh) {
    newLeft[c.deck_id] ??= Math.max(0, (own(c.deck_id).perDay ?? limits.perDay) - (limits.used[c.deck_id] ?? 0));
    if (newLeft[c.deck_id] > 0 && room(c)) { newLeft[c.deck_id]--; add(c); }
  }
  return plan;
}
