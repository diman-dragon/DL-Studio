export default function projectStudyStats({ cards, due, overdue, late }) {
  const count = Number(overdue || 0), old = Number(late || 0);
  const warning = count
    ? `<span class="overdue ${old ? 'overdue-hot' : ''}">${old ? '⚠ ' : ''}${count} просрочено</span>`
    : '<span class="overdue ok">нет просрочки</span>';
  return `<span class="project-stats ${count ? 'late' : ''}">${warning}</span>`;
}
