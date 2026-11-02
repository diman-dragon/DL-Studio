import button from './button.js';
/** Значок-кнопка в строке списка (карандаш, корзина): act — действие, id — элемент, svg — готовый значок. */
export default function iconButton(act, aria, svg, id) { return button({ act, cls: 'ib', label: svg, aria, data: { id } }); }
