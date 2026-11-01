import { state } from '../state/appState.js';
import { track } from '../state/trackState.js';
import toggleFlip from '../dom/toggleFlip.js';
import buzz from '../dom/buzz.js';
import trackFlip from '../study/trackFlip.js';
import autoSpeak from '../study/autoSpeak.js';
/** Переворот сколько угодно раз; каждый переворот — сигнал для оценки; озвучка по ответу — при первом открытии. */
export default async function flip() {
  const showingBack = toggleFlip();
  buzz();
  trackFlip();
  if (state.quiz?.kind === 'speak' && !state.speech) track.hinted = true;   // посмотрел текст до попытки — это подсказка
  if (!showingBack || state.revealed) return;
  state.revealed = true;
  await autoSpeak('reveal');
}
