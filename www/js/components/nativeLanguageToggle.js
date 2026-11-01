import { state } from '../state/appState.js';
import button from './button.js';

const LANGS = [['system', 'Системный'], ['ru', 'Русский'], ['sr', 'Србски'], ['en', 'English']];
function systemLanguage() {
  const code = (globalThis.navigator?.language || '').toLowerCase();
  return code.startsWith('sr') ? 'Србски' : code.startsWith('en') ? 'English' : 'Русский';
}
export default function nativeLanguageToggle() {
  const current = state.prefs.nativeLang || 'system';
  const label = current === 'system' ? `Системный (${systemLanguage()})` : LANGS.find(([id]) => id === current)?.[1] || 'Системный';
  return button({ act: 'cycleNativeLang', cls: 'toggle', label: `${label} →`, aria: 'Сменить нативный язык' });
}
