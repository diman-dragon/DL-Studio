import { state } from '../state/appState.js';
import { SPEECH } from '../constants/SPEECH.js';
import speechListen from '../native/speechListen.js';
import prepareSpeech from '../study/prepareSpeech.js';
import insertPronunciation from '../repo/insertPronunciation.js';
import currentCard from '../study/currentCard.js';
import scoreSpeech from '../study/scoreSpeech.js';
import speechResult from '../components/speechResult.js';
import fitFaces from '../dom/fitFaces.js';
import buzz from '../dom/buzz.js';
import toast from '../dom/toast.js';
import spokenText from '../study/spokenText.js';
/** Микрофон в «Говорить»: слушаем одну попытку, оцениваем сходство с текстом карточки, пишем попытку в базу (pronunciations). */
export default async function record(d, el) {
  const card = state.session && currentCard(state.session);
  if (!card || state.listening) return;
  const box = document.getElementById('speechres');
  state.listening = true;
  el?.classList?.add('rec');
  box.innerHTML = '<div class="sres">Слушаю…</div>';
  try {
    await prepareSpeech(card.tts);
    const r = await speechListen(card.tts);
    const target = state.quiz?.kind === 'speak' ? state.quiz.answer : spokenText(card);
    const score = scoreSpeech(r, target), prev = state.speech?.id === card.id ? state.speech.attempts : 0;
    await insertPronunciation(card.id, { engine: SPEECH.engine, transcript: r.text, score, audio: r.audio ?? null });
    state.speech = { id: card.id, heard: r.text, score, attempts: prev + 1 };
    box.innerHTML = speechResult(state.speech);
    fitFaces();
    buzz(score >= SPEECH.good ? 12 : [30, 40, 30]);
  } catch (e) {
    box.innerHTML = state.speech?.id === card.id ? speechResult(state.speech) : '';
    toast(e.message || 'Не удалось распознать речь');
    if (/пакет/.test(e.message || '')) prepareSpeech(card.tts, true).then(t => t && toast(t));   // нет пакета языка — проверяем и скачиваем прямо отсюда
  } finally {
    state.listening = false;
    el?.classList?.remove('rec');
  }
}
