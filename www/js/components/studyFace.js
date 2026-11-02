import esc from '../util/esc.js';
import { QUIZ_ASK } from '../constants/QUIZ_ASK.js';
import flipFaces from './flipFaces.js';
import speakControls from './speakControls.js';
import audioPrompt from './audioPrompt.js';
import exampleBlock from './exampleBlock.js';
import big from './big.js';
import quizVerdict from './quizVerdict.js';
import spokenText from '../study/spokenText.js';
/**
 * Лицо — вопрос; оборот — вопрос, ответ, пример. Язык изучаемого слова — front. quiz — вопрос с выбором / вводом (тап не переворачивает).
 * Если долго думают, не переворачивая, показывается пример (.peek, см. study/peekLater); в задании «на слух» — кнопка «Показать текстом».
 */
export default function studyFace(c, flipped, dir, quiz = null) {
  const fwd = dir === 'fwd';
  const family = quiz?.kind?.split('-')[0], audio = family === 'audio', cloze = family === 'cloze';
  const prompt = cloze ? quiz.prompt : fwd ? c.front : c.back;
  const answer = quiz?.answer || (fwd ? c.back : c.front), sample = fwd ? c.example : c.example_tr;
  const peek = !quiz && sample ? `<div class="ex peek" hidden>${esc(sample)}</div>` : '';
  const hint = quiz ? QUIZ_ASK[quiz.kind] : 'Тап — перевернуть · свайп влево — дальше, вправо — назад';
  const shown = audio ? audioPrompt(c) : big(prompt) + (fwd ? speakControls(spokenText(c), c.tts) : '');
  const front = `${shown}${peek}<div class="muted hint">${hint}</div>`;
  const back = `<div class="muted">${esc(prompt)}</div>${big(answer)}<div id="verdict">${quizVerdict(quiz)}</div>${speakControls(spokenText(c), c.tts)}${exampleBlock(c)}` +
    `<div class="muted hint">${quiz ? 'Свайп влево — дальше, вправо — назад' : 'Тап — вернуться · свайп влево — дальше, вправо — назад'}</div>`;
  return flipFaces(front, back, flipped, quiz ? '' : 'flip');
}
