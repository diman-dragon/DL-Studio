import button from '../components/button.js';
import row from '../components/row.js';
/** Только листание и переворот: оценки кнопками нет — расписание считается по поведению (свайпы, перевороты, время). */
export default function studyControls() {
  return row(button({ act: 'prev', cls: 'icon', label: '‹' }) + button({ act: 'flip', cls: 'p wide', label: 'Перевернуть' }) + button({ act: 'skip', cls: 'icon', label: '›' }), 'nav-row');
}
