import navActions from './navActions.js';
import libraryActions from './libraryActions.js';
import studyActions from './studyActions.js';
import dataActions from './dataActions.js';
/** Все действия интерфейса: data-act="<имя>" (правила добавления — docs/CONVENTIONS.md). */
export default { ...navActions, ...libraryActions, ...studyActions, ...dataActions };
