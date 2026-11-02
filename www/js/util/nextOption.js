/** Следующее значение по кругу: options = [[значение, подпись], ...]; неизвестное текущее — первое. */
export default function nextOption(options, current) {
  const i = options.findIndex(([v]) => v === String(current));
  return options[(i + 1) % options.length][0];
}
