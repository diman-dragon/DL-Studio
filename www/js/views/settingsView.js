import { APP_VERSION } from '../constants/APP_VERSION.js';
import { APP_NAME } from '../constants/APP_NAME.js';
import header from '../components/header.js';
import button from '../components/button.js';
import logoMark from '../components/logoMark.js';
import pagedList from '../components/pagedList.js';
import prefControl from '../components/prefControl.js';
import settingRow from '../components/settingRow.js';
import settingsGroup from '../components/settingsGroup.js';
import row from '../components/row.js';

/** Настройки новой модели: нативный язык, SRS, лимиты, формат JSON, данные и внешний вид. */
export default async function settingsView() {
  const look = settingRow('Тема', prefControl('theme'), 'AMOLED — чистый чёрный фон') + settingRow('Размер текста на карточках', prefControl('font')) +
    settingRow('Анимации', prefControl('anim')) + settingRow('Вибрация', prefControl('haptic'), 'Лёгкий отклик на свайп и переворот');
  const study = settingRow('Нативный язык', prefControl('nativeLang'), 'Язык перевода карточек') +
    settingRow('Автоозвучка', prefControl('autoplay'), 'Произносить изучаемую сторону при появлении карточки') +
    settingRow('Задания', '<div class="muted">Автоматически чередуются переворот, выбор, ввод и произношение.</div>');
  const limits = settingRow('Новых карточек в день', prefControl('newPerDay'), 'Сколько новых карточек добавлять в план каждой колоды') +
    settingRow('Карточек за сессию', prefControl('sessionMax'), 'Сначала идут просроченные и самые давние карточки');
  const data = settingRow('Библиотека', row(button({ act: 'importLibrary', label: 'Файл' }) + button({ act: 'importPaste', label: 'Текст' }) + button({ act: 'exportLibrary', label: 'Экспорт' })), 'Импорт: JSON, CSV, TSV, TXT, Anki, Quizlet и Excel') +
    settingRow('Резервная копия (.fcbak)', row(button({ act: 'backup', cls: 'p', label: 'Создать' }) + button({ act: 'restore', label: 'Восстановить' })), 'Перед восстановлением текущая база сохраняется') +
    settingRow('Прогресс', button({ act: 'resetProgress', cls: 'd', label: 'Сбросить прогресс обучения' }), 'Карточки и колоды останутся');
  const about = settingRow(APP_NAME, `<div class="about"><span class="brand">${logoMark()}</span><div class="muted">Версия ${APP_VERSION}<br>Офлайн-интервальные повторения<br>© DL-Studio</div></div>`);
  const json = `<pre class="format-code">{
  "format": "lingvadesk-library",
  "version": 1,
  "native_language": "ru",
  "projects": [{
    "name": "English",
    "tts": "en-US",
    "decks": [{
      "name": "Basics",
      "cards": [{
        "front": "apple",
        "back": "яблоко",
        "example": "I eat an apple.",
        "example_translation": "Я каждый день ем яблоко."
      }]
    }]
  }]
}</pre>`;
  const pages = [['Вид', look], ['Учёба', study], ['Лимиты', limits], ['Формат JSON', settingRow('Формат проекта', json, 'front — изучаемый язык; back — перевод на нативный язык; example и example_translation — необязательные фразы')], ['Данные', data], ['О нас', about]];
  return header('Настройки') + pagedList(pages.map(([title, rows]) => settingsGroup(title, rows)), { fixed: true, titles: pages.map(p => p[0]) });
}
