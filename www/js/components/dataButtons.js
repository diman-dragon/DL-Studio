import button from './button.js';
const BUTTONS = [
  ['importLibrary', 'Из файла', 'Импорт библиотеки из файла: JSON, CSV, TSV, TXT, Anki, Quizlet, Excel'], ['importPaste', 'Из текста', 'Импорт библиотеки из вставленного текста'],
  ['exportLibrary', 'Экспорт', 'Экспорт всей библиотеки'], ['backup', 'Копия', 'Создать резервную копию .fcbak'],
  ['restore', 'Из копии', 'Восстановить из копии (текущая база сохранится)'], ['resetProgress', 'Сброс', 'Сбросить прогресс обучения (карточки останутся)', 'd']
];
/** Данные: импорт, экспорт, резервная копия, сброс; формат JSON и сведения о приложении открываются нижним окном. */
export default function dataButtons() {
  const own = BUTTONS.map(([act, label, aria, cls = '']) => button({ act, cls, label, aria }));
  const sheets = [['json', 'Формат JSON'], ['about', 'О приложении']].map(([id, label]) => button({ act: 'openSheet', label, data: { id } }));
  return `<h3>Данные</h3><div class="data-grid">${own.join('')}${sheets.join('')}</div>`;
}
