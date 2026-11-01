export default function projectStudyMeta({ cards, due, overdue }) {
  return `${cards} карточек\n${due} к повторению\n${Number(overdue || 0)} просрочено`;
}
