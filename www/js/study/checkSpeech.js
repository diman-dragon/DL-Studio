import speechPack from '../native/speechPack.js';
/** Нет распознавания речи на устройстве — session.noSpeech: задание «произношение» не выдаётся (study/quizPick). Не удалось узнать — считаем, что есть. */
export default async function checkSpeech(session) {
  const r = await speechPack('available', '').catch(() => null);
  session.noSpeech = r?.available === false;
}
