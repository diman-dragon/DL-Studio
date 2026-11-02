import go from '../nav/go.js';
/** Нажатие на проект в «Учёбе» — его колоды (у каждой своё повторение). */
export default function goStudyProject(d) { return go('studyDecks', { id: +d.id }); }
