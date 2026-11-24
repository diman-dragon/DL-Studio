// Рисует портреты персонажей из открытого набора DiceBear «Micah» (автор Micah Lanier, лицензия CC BY 4.0) и кладёт их в res/drawable-nodpi.
// Кадры одного персонажа отличаются только ртом и глазами: покой, моргание, два кадра речи, радость, огорчение.
// Запуск (нужны npm-пакеты и Playwright с Chromium; python3 с Pillow):
//   cd scripts && npm i --no-save @dicebear/core@9 @dicebear/collection@9 playwright && node portraits.mjs
// Пишет: ../app/src/main/res/drawable-nodpi/ch_<id>_<кадр>.webp, ../app/src/main/java/com/dlstudio/app/ui/components/PortraitRes.kt, ../licenses/Micah-CC-BY-4.0.txt
import { mkdirSync, writeFileSync } from 'node:fs';
import { execFileSync } from 'node:child_process';
import { join, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';
import { createAvatar } from '@dicebear/core';
import { micah } from '@dicebear/collection';
import { chromium } from 'playwright';

const root = join(dirname(fileURLToPath(import.meta.url)), '..');
const out = join(root, 'app/src/main/res/drawable-nodpi');
const SIZE = 384;
const INK = '000000';

// Внешность каждого персонажа: одна запись — один человек (см. Characters.kt).
const LOOKS = {
  grace: { baseColor: ['f9c9b6'], hair: ['full'], hairColor: ['77311d'], hairProbability: 100, glasses: ['round'], glassesColor: [INK], glassesProbability: 100, earrings: ['stud'], earringColor: ['f4d150'], earringsProbability: 100, eyebrows: ['up'], shirt: ['collared'], shirtColor: ['e0ddff'], facialHairProbability: 0, nose: ['pointed'], ears: ['attached'] },
  leo: { baseColor: ['ac6651'], hair: ['fonze'], hairColor: ['77311d'], hairProbability: 100, glassesProbability: 0, earringsProbability: 0, eyebrows: ['up'], shirt: ['crew'], shirtColor: ['f4d150'], facialHair: ['beard'], facialHairColor: ['77311d'], facialHairProbability: 100, nose: ['curve'], ears: ['attached'] },
  beatrice: { baseColor: ['f9c9b6'], hair: ['pixie'], hairColor: ['9287ff'], hairProbability: 100, glassesProbability: 0, earrings: ['hoop'], earringColor: ['fc909f'], earringsProbability: 100, eyebrows: ['eyelashesUp'], shirt: ['crew'], shirtColor: ['6bd9e9'], facialHairProbability: 0, nose: ['tound'], ears: ['detached'] },
  john: { baseColor: ['77311d'], hair: ['dougFunny'], hairColor: [INK], hairProbability: 100, glasses: ['square'], glassesColor: ['6bd9e9'], glassesProbability: 100, earringsProbability: 0, eyebrows: ['down'], shirt: ['open'], shirtColor: ['9287ff'], facialHairProbability: 0, nose: ['curve'], ears: ['detached'] },
  narrator: { baseColor: ['f9c9b6'], hair: ['mrClean'], hairColor: ['ffffff'], hairProbability: 100, glasses: ['square'], glassesColor: ['77311d'], glassesProbability: 100, earringsProbability: 0, eyebrows: ['down'], shirt: ['collared'], shirtColor: ['ffeba4'], facialHairProbability: 0, nose: ['pointed'], ears: ['attached'] }
};
// Кадры: глаза и рот.
const FRAMES = {
  idle: { eyes: ['eyes'], mouth: ['smile'] },
  blink: { eyes: ['smiling'], mouth: ['smile'] },
  talk1: { eyes: ['eyes'], mouth: ['laughing'] },
  talk2: { eyes: ['eyes'], mouth: ['pucker'] },
  happy: { eyes: ['smiling'], mouth: ['laughing'] },
  sad: { eyes: ['eyes'], mouth: ['sad'], eyebrows: ['down'] }
};

mkdirSync(out, { recursive: true });
const browser = await chromium.launch();
const page = await browser.newPage({ viewport: { width: SIZE, height: SIZE } });
const lines = [];
for (const [id, look] of Object.entries(LOOKS)) {
  const row = [];
  for (const [frame, change] of Object.entries(FRAMES)) {
    const svg = createAvatar(micah, { seed: id, size: SIZE, backgroundColor: [], mouthColor: [INK], eyesColor: [INK], eyebrowsColor: [INK], ...look, ...change }).toString();
    await page.setContent(`<body style="margin:0;background:transparent">${svg}</body>`);
    const png = join(out, `ch_${id}_${frame}.png`);
    await page.screenshot({ path: png, omitBackground: true, clip: { x: 0, y: 0, width: SIZE, height: SIZE } });
    execFileSync('python3', ['-c', `from PIL import Image;Image.open(${JSON.stringify(png)}).save(${JSON.stringify(png.replace('.png', '.webp'))}, 'WEBP', quality=88, method=6)`]);
    execFileSync('rm', [png]);
    row.push(`"${frame}" to R.drawable.ch_${id}_${frame}`);
  }
  lines.push(`        "${id}" to mapOf(${row.join(', ')})`);
}
await browser.close();

writeFileSync(join(root, 'app/src/main/java/com/dlstudio/app/ui/components/PortraitRes.kt'), `package com.dlstudio.app.ui.components

import com.dlstudio.app.R

/** Кадры портретов персонажей (создаётся скриптом scripts/portraits.mjs; не править вручную). Ресурсы названы явно, чтобы сжатие ресурсов их не вырезало. */
object PortraitRes {
    private val frames: Map<String, Map<String, Int>> = mapOf(
${lines.join(',\n')}
    )

    /** Ресурс кадра персонажа; нет такого кадра — спокойный. */
    fun of(id: String, frame: String): Int? = frames[id]?.let { it[frame] ?: it["idle"] }
}
`);
console.log('готово:', Object.keys(LOOKS).length, 'персонажей');
