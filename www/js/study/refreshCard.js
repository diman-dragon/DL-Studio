/** После правки карточки из сессии обновляем её текст в сессии (без перезапуска). card — поля формы (front, back, example, exampleTr). */
export default function refreshCard(session, id, card) {
  const c = session?.map.get(id);
  if (session?.tasks) delete session.tasks[id];   // текст изменился — задание строится заново
  if (c) Object.assign(c, { front: card.front, back: card.back, example: card.example || null, example_tr: card.exampleTr || null,
    cloze: card.cloze || null, cloze_answer: card.clozeAnswer || null, cloze_tr: card.clozeTr || null });
}
