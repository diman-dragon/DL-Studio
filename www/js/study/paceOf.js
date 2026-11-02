/** Темп интервалов колоды карточки (множитель дней до следующего повторения); своего нет — 1. */
export default function paceOf(session, card) { return session.limits?.decks?.[card.deck_id]?.pace ?? 1; }
