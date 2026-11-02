import { FIT_MIN } from '../constants/FIT_MIN.js';
import fitOpts from './fitOpts.js';
import fitWords from './fitWords.js';
/**
 * Подгоняет текст под доступное место, ничего не обрезая:
 * — карточка (.face): пока содержимое не помещается, масштаб --fit уменьшается шагами по 5 % (текст, пояснения, кнопка озвучки);
 * — страница списка (.page), если один элемент выше области (огромный текст), масштабируется целиком (zoom).
 * Заодно подгоняет варианты ответа (dom/fitOpts) и не даёт рваться словам по буквам (dom/fitWords).
 * Вызывается после отрисовки и когда содержимое меняется.
 */
export default function fitFaces() {
  fitOpts();
  for (const face of document.querySelectorAll?.('.face') ?? []) {
    let k = 1;
    face.style.removeProperty('--fit');
    while (face.scrollHeight > face.clientHeight + 1 && k > FIT_MIN) { k = Math.round((k - 0.05) * 100) / 100; face.style.setProperty('--fit', k); }
  }
  fitWords();
  const pager = document.getElementById?.('pager'), page = pager?.querySelector?.('.page');
  if (!page || !pager.clientHeight) return;
  let z = 1;
  page.style.zoom = '';
  while (page.getBoundingClientRect().height > pager.clientHeight + 1 && z > FIT_MIN) { z = Math.round((z - 0.05) * 100) / 100; page.style.zoom = z; }
}
