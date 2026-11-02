import button from './button.js';
/** Значок ⓘ: открывает краткую справку по экрану (нижнее окно). */
export default function infoButton() { return button({ act: 'openSheet', cls: 'icon info', label: 'ⓘ', aria: 'Как пользоваться экраном', data: { id: 'help' } }); }
