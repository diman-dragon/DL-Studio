import esc from '../util/esc.js';
import { QUIZ_ASK } from '../constants/QUIZ_ASK.js';
import flipFaces from '../components/flipFaces.js';
import speakButton from '../components/speakButton.js';
import exampleBlock from '../components/exampleBlock.js';
import big from '../components/big.js';
import quizVerdict from './quizVerdict.js';
/**
 * Лицо — вопрос; оборот — вопрос, ответ, пример. Язык изучаемого слова — front. quiz — вопрос с выбором / вводом (тап не переворачивает).
 * Если долго думают, не переворачивая, показывается пример (.peek, см. study/peekLater).
 */
export default function studyFace(c, flipped, dir, quiz = null) {
  const fwd = dir === 'fwd';
  const prompt = fwd ? c.front : c.back, answer = fwd ? c.back : c.front, sample = fwd ? c.example : c.example_tr;
  const peek = !quiz && sample ? `<div class="ex peek" hidden>${esc(sample)}</div>` : '';
  const hint = quiz ? QUIZ_ASK[quiz.kind] : 'Тап — перевернуть · свайп влево — дальше, вправо — назад';
  const front = `${big(prompt)}${fwd ? speakButton(c.front, c.tts) : ''}${peek}<div class="muted hint">${hint}</div>`;
  const back = `<div class="muted">${esc(prompt)}</div>${big(answer)}<div id="verdict">${quizVerdict(quiz)}</div>${speakButton(c.front, c.tts)}${exampleBlock(c)}` +
    `<div class="muted hint">${quiz ? 'Свайп влево — дальше, вправо — назад' : 'Тап — вернуться · свайп влево — дальше, вправо — назад'}</div>`;
  return flipFaces(front, back, flipped, quiz ? '' : 'flip');
}
