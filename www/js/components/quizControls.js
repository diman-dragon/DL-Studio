import button from './button.js';
import optGrid from './optGrid.js';
import row from './row.js';
import answerInput from './answerInput.js';
const next = () => row(button({ act: 'prev', cls: 'icon', label: '‹' }) + button({ act: 'skip', cls: 'p wide', label: 'Дальше' }), 'nav-row');
const mark = (q, i) => (q.options[i] === q.answer ? ' ok' : q.picked === i ? ' bad' : '');
/** Под карточкой: варианты ответа (2 × 2) или поле ввода; после ответа — «Дальше». */
export default function quizControls(q) {
  if (q.kind === 'choice') {
    return optGrid(q.options, 'pick', i => (q.result ? mark(q, i) : '')) + (q.result ? next() : row(button({ act: 'giveUp', cls: 'sm', label: 'Не знаю' }), 'nav-row'));
  }
  if (q.result) return next();
  return answerInput(q) +
    row(button({ act: 'giveUp', cls: 'sm', label: 'Не знаю' }) + button({ act: 'hint', cls: 'sm', label: 'Подсказка' }) + button({ act: 'check', cls: 'p wide', label: 'Проверить' }), 'nav-row');
}
