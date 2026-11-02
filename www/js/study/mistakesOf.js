/** id карточек, оценённых «Снова» в этой сессии. */
export default function mistakesOf(session) { return session.log.filter(l => l.grade === 'again').map(l => l.id); }
