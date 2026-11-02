package com.example.lingocards.speech;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.speech.SpeechRecognizer;
import com.example.lingocards.core.AtomicPlugin;
import com.getcapacitor.JSObject;
import com.getcapacitor.PermissionState;
import com.getcapacitor.PluginCall;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.getcapacitor.annotation.PermissionCallback;

/**
 * Проверка произношения (сервисы Google). action: "listen" (по умолчанию) — одна попытка → { text, confidence (-1 — неизвестна), alternatives[], audio (null) };
 * "check" — есть ли офлайн-пакет языка → { checked, installed, supported, pending, online }; "available" — есть ли на устройстве распознавание речи → { available }; "download" — скачать пакет
 * (Android 13+; раньше открываются настройки голосового ввода). Записи не сохраняются.
 */
@CapacitorPlugin(name = "SpeechListen", permissions = @Permission(alias = "mic", strings = { Manifest.permission.RECORD_AUDIO }))
public class SpeechListenPlugin extends AtomicPlugin {
    @Override protected JSObject handle(PluginCall c) {
        String action = c.getString("action", "listen"), lang = c.getString("lang", "en-US");
        if ("available".equals(action)) return new JSObject().put("available", SpeechRecognizer.isRecognitionAvailable(getActivity()));
        if ("listen".equals(action)) {
            if (getPermissionState("mic") == PermissionState.GRANTED) SpeechStart.run(getActivity(), c, true);
            else requestPermissionForAlias("mic", c, "onPermission");
            return null;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { SpeechSupport.run(getActivity(), c, lang, "download".equals(action)); return null; }
        if ("download".equals(action)) getActivity().startActivity(new Intent("android.settings.VOICE_INPUT_SETTINGS"));
        return new JSObject().put("checked", false).put("settings", "download".equals(action));
    }

    @PermissionCallback
    private void onPermission(PluginCall c) {
        if (getPermissionState("mic") == PermissionState.GRANTED) SpeechStart.run(getActivity(), c, true);
        else c.reject("Нет доступа к микрофону: разрешите его в настройках приложения");
    }
}
