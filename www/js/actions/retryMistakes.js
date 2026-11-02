import studyStep from '../study/studyStep.js';
import retrySession from '../study/retrySession.js';
/** «Повторить ошибки» в конце сессии: карточки, оценённые «Снова», ещё одним кругом без изменения расписания. */
export default function retryMistakes() { return studyStep(retrySession); }
