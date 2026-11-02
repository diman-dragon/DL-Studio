import test from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import views from '../www/js/views/index.js';
import { HELP } from '../www/js/constants/HELP.js';
import helpSheet from '../www/js/components/helpSheet.js';

test('приложение только в книжной ориентации', () => {
  assert.match(readFileSync('android/app/src/main/AndroidManifest.xml', 'utf8'), /android:screenOrientation="portrait"/);
});

test('справка есть у каждого экрана, кроме настроек', () => {
  for (const v of Object.keys(views)) assert.equal(v in HELP, v !== 'settings', v);
  assert.match(helpSheet('session'), /Как пользоваться/);
  assert.equal(helpSheet('settings'), '');
});
