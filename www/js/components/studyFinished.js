import faceBox from './faceBox.js';
import button from './button.js';
import row from './row.js';
import sessionSummary from './sessionSummary.js';
import mistakesOf from '../study/mistakesOf.js';
/** Конец сессии: итог; если были «Снова» — «Повторить ошибки (N)» ещё одним кругом без изменения расписания. */
export default function studyFinishedView(session) {
  const mistakes = mistakesOf(session).length;
  const text = !session.total ? 'Нет карточек' : session.retry ? (mistakes ? 'Круг пройден' : 'Ошибки разобраны 🎉') : 'Всё повторено! 🎉';
  const again = mistakes ? button({ act: 'retryMistakes', cls: 'p', label: `Повторить ошибки (${mistakes})` }) : '';
  return faceBox(`<div class="big">${text}</div>${sessionSummary(session)}`) +
    row(button({ act: 'back', label: 'К учёбе' }) + again + button({ act: 'restart', cls: again ? '' : 'p', label: 'Обновить' }));
}
