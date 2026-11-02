/** { pct, text } для полосы прогресса. «Карточки» — номер по кругу; иначе сколько карточек плана осталось (сессия = план, сверх него ничего нет). */
export default function progressOf(s) {
  const n = s.order.length;
  if (s.mode === 'cards') return { pct: (s.pos + 1) / n * 100, text: `Карточка ${s.pos + 1} из ${n}` };
  return { pct: s.total ? (s.total - n) / s.total * 100 : 100, text: n ? `Осталось ${n} из ${s.total}` : '✓ План выполнен' };
}
