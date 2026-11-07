package com.dlstudio.app.study

import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.speech.SpeechRecognizer

/**
 * Способы распознавания по порядку: 1) на устройстве (Android 13+), 2) служба по умолчанию, 3) служба Google напрямую —
 * она работает, даже когда ассистентом по умолчанию выбран Gemini или другое приложение и `isRecognitionAvailable` говорит «нет».
 */
object RecognizerEngines {
    private val google = ComponentName("com.google.android.googlequicksearchbox", "com.google.android.voicesearch.serviceapi.GoogleRecognitionService")

    fun all(context: Context): List<RecognizerEngine> = buildList {
        if (Build.VERSION.SDK_INT >= 33 && SpeechRecognizer.isOnDeviceRecognitionAvailable(context)) add(RecognizerEngine(true) { SpeechRecognizer.createOnDeviceSpeechRecognizer(context) })
        if (SpeechRecognizer.isRecognitionAvailable(context)) add(RecognizerEngine(false) { SpeechRecognizer.createSpeechRecognizer(context) })
        if (runCatching { context.packageManager.getServiceInfo(google, 0) }.isSuccess) add(RecognizerEngine(false) { SpeechRecognizer.createSpeechRecognizer(context, google) })
    }
}
