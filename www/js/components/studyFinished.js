import faceBox from './faceBox.js';
import button from './button.js';
import row from './row.js';
import sessionSummary from './sessionSummary.js';
const DONE = 'Всё повторено! 🎉', NONE = 'Нет карточек', AGAIN = 'Обновить';
export default function studyFinishedView(session) {
  const text = session.total ? DONE : NONE;
  return faceBox(`<div class="big">${text}</div>${sessionSummary(session)}`) +
    row(button({ act: 'back', label: 'К учёбе' }) + button({ act: 'restart', cls: 'p', label: AGAIN }));
}
