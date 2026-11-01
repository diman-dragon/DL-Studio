import { state } from '../state/appState.js';
import { DEFAULT_TTS } from '../constants/DEFAULT_TTS.js';
import getProject from '../repo/getProject.js';
import header from '../components/header.js';
import backButton from '../components/backButton.js';
import labeledField from '../components/labeledField.js';
import ttsCodes from '../components/ttsCodes.js';
import formButtons from '../components/formButtons.js';
export default async function projectFormView() {
  const id = state.nav.id;
  const c = id ? await getProject(id) : { name: '', tts: DEFAULT_TTS };
  return header(id ? 'Проект' : 'Новый проект', { left: backButton('back') }) +
    labeledField({ id: 'name', label: 'Название', value: c.name, placeholder: 'Например, Юридический английский' }) +
    labeledField({ id: 'tts', label: 'Изучаемый язык (код для озвучки)', value: c.tts, placeholder: 'en-US', list: 'tts-codes' }) + ttsCodes() +
    formButtons({ save: 'saveProject', del: id ? 'delProject' : '' });
}
