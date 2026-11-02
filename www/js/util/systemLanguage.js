/** Название языка системы среди поддерживаемых нативных языков (по умолчанию русский). */
export default function systemLanguage() {
  const code = (globalThis.navigator?.language || '').toLowerCase();
  return code.startsWith('sr') ? 'Србски' : code.startsWith('en') ? 'English' : 'Русский';
}
