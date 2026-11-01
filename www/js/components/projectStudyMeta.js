export default function projectStudyMeta({ cards, due, overdue }) {
  return `${cards} карточек · ${due} к повторению · ${Number(overdue || 0)} просрочено`;
}
