import esc from '../util/esc.js';
import flipFaces from './flipFaces.js';
import speakButton from './speakButton.js';
import exampleBlock from './exampleBlock.js';
import big from './big.js';
import speechPrompt from './speechPrompt.js';
/**
 * «Говорить»: на лице — перевод и большой микрофон: нужно произнести слово (фразу) на изученном языке; итог попытки — под микрофоном.
 * «Показать текст» и «Послушать» — подсказки (оценка опускается). Оборот — текст, перевод, пример.
 */
export default function speakFace(c, flipped, speech = null, quiz = null) {
  const front = speechPrompt(c, speech, quiz);
  const phrase = quiz?.answer || c.front;
  const back = `${big(phrase)}${big(c.back)}${speakButton(phrase, c.tts)}${exampleBlock(c)}` +
    '<div class="muted hint">Тап — вернуться · свайп влево — дальше, вправо — назад</div>';
  return flipFaces(front, back, flipped);
}
