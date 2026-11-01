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
/**
 * Настройки соответствуют новой модели: нативный язык, типы заданий SRS, лимиты, данные и внешний вид.
 */
export default async function settingsView() {
  const look = settingRow('Тема', prefControl('theme'), 'AMOLED — чистый чёрный фон') + settingRow('Размер текста на карточках', prefControl('font')) +
    settingRow('Анимации', prefControl('anim')) + settingRow('Вибрация', prefControl('haptic'), 'Лёгкий отклик на свайп и переворот');
  const study = settingRow('Нативный язык', prefControl('nativeLang'), 'Используется как язык перевода и подсказок') +
    settingRow('Тип задания', prefControl('check'), 'В интервальном повторении доступны переворот, выбор, ввод и произношение') +
    settingRow('Автоозвучка', prefControl('autoplay'), 'Произносить слово, когда оно появляется') +
    settingRow('Озвучка', '<div class="muted">Язык карточки задаётся в проекте. Голос выбирается автоматически.</div>');
  const limits = settingRow('Новых карточек в день', prefControl('newPerDay'), 'Сколько новых слов добавляет интервальное повторение за день') +
    settingRow('Карточек за сессию', prefControl('sessionMax'), 'Просроченные идут первыми, самые давние — раньше');
  const data = settingRow('Библиотека', row(button({ act: 'importLibrary', label: 'Файл' }) + button({ act: 'importPaste', label: 'Текст' }) + button({ act: 'exportLibrary', label: 'Экспорт' })), 'Импорт: CSV, TSV, TXT, Anki (.txt, .apkg), Quizlet, Excel, JSON; дублей не создаёт') +
    settingRow('Резервная копия (.fcbak)', row(button({ act: 'backup', cls: 'p', label: 'Создать' }) + button({ act: 'restore', label: 'Восстановить' })),
      'Перед восстановлением текущая БД сохраняется в защитную копию') +
    settingRow('Прогресс', button({ act: 'resetProgress', cls: 'd', label: 'Сбросить прогресс обучения' }), 'Карточки и колоды останутся; история повторений очистится');
  const about = settingRow(APP_NAME, `<div class="about"><span class="brand">${logoMark()}</span><div class="muted">Версия ${APP_VERSION}<br>Офлайн-интервальные повторения<br>© DL-Studio</div></div>`);
  const json = `<pre class="format-code">{\n  "format": "lingvadesk-library",\n  "version": 1,\n  "native_language": "ru",\n  "projects": [{\n    "name": "English",\n    "tts": "en-US",\n    "decks": [{\n      "name": "Basics",\n      "cards": [{\n        "front": "apple",\n        "back": "яблоко",\n        "example": "I eat an apple."\n      }]\n    }]\n  }]\n}</pre>`;
  const pages = [['Вид', look], ['Учёба', study], ['Лимиты', limits], ['Формат JSON', settingRow('Формат проекта', json, 'front — изучаемый язык; back — перевод; example и example_translation — необязательные фразы')], ['Данные', data], ['О нас', about]];
  return header('Настройки') +
    pagedList(pages.map(([title, rows]) => settingsGroup(title, rows)), { fixed: true, titles: pages.map(p => p[0]) });
}
