import { state } from '../state/appState.js';
import header from '../components/header.js';
import settingTiles from '../components/settingTiles.js';
import dataButtons from '../components/dataButtons.js';
import settingsSheet from '../components/settingsSheet.js';
/** Настройки одним экраном: плитки-переключатели (нажатие — следующее значение по кругу), данные, нижние окна «Формат JSON» и «О приложении». */
export default async function settingsView() {
  return header('Настройки') + `<div class="settings">${settingTiles()}${dataButtons()}</div>` + settingsSheet(state.sheet);
}
