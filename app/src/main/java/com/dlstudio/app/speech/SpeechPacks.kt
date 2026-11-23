package com.dlstudio.app.speech

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.speech.RecognitionSupport
import android.speech.RecognitionSupportCallback
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat

/** Состояние офлайн-пакета распознавания речи для языка: неизвестно, скачан, можно скачать, качается, недоступен. */
enum class PackState { UNKNOWN, INSTALLED, AVAILABLE, DOWNLOADING, UNSUPPORTED }

/**
 * Языковые пакеты встроенных средств Android: голоса синтеза речи (скачиваются в настройках движка) и офлайн-распознавание речи
 * (на Android 13+ скачивается прямо отсюда, на старых — в настройках голосового ввода). Своих движков и моделей у приложения нет.
 */
object SpeechPacks {
    private fun open(context: Context, vararg intents: Intent): Boolean = intents.any { intent ->
        runCatching { context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }.isSuccess
    }

    /** Открывает загрузку голосовых данных движка синтеза речи; если движок такого экрана не имеет — настройки синтеза речи. */
    fun openVoiceDownload(context: Context): Boolean = open(context, Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA), Intent(TTS_SETTINGS), Intent(Settings.ACTION_SETTINGS))

    /** Открывает настройки синтеза речи (движок, голоса, скорость). */
    fun openSpeechSettings(context: Context): Boolean = open(context, Intent(TTS_SETTINGS), Intent(Settings.ACTION_SETTINGS))

    /** Открывает настройки голосового ввода, где на старых Android скачиваются офлайн-пакеты распознавания. */
    fun openRecognitionSettings(context: Context): Boolean = open(context, Intent(Settings.ACTION_VOICE_INPUT_SETTINGS), Intent(Settings.ACTION_SETTINGS))

    /** Умеет ли это устройство проверять и скачивать офлайн-пакеты распознавания само (Android 13+ и встроенное распознавание на устройстве). */
    fun canManage(context: Context): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && SpeechRecognizer.isOnDeviceRecognitionAvailable(context)

    private fun intentFor(tag: String) = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        .putExtra(RecognizerIntent.EXTRA_LANGUAGE, tag)

    /** Узнаёт, скачан ли пакет языка `tag`. Вызывать с главного потока; результат придёт туда же. */
    fun check(context: Context, tag: String, onResult: (PackState) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && SpeechRecognizer.isOnDeviceRecognitionAvailable(context)) checkOnDevice(context, tag, onResult)
        else onResult(PackState.UNSUPPORTED)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun checkOnDevice(context: Context, tag: String, onResult: (PackState) -> Unit) {
        val engine = SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
        engine.checkRecognitionSupport(intentFor(tag), ContextCompat.getMainExecutor(context), object : RecognitionSupportCallback {
            override fun onSupportResult(recognitionSupport: RecognitionSupport) {
                fun has(list: List<String>) = list.any { it.replace('_', '-').equals(tag, ignoreCase = true) || VoiceNames.language(it) == VoiceNames.language(tag) }
                onResult(
                    when {
                        has(recognitionSupport.installedOnDeviceLanguages) -> PackState.INSTALLED
                        has(recognitionSupport.pendingOnDeviceLanguages) -> PackState.DOWNLOADING
                        has(recognitionSupport.supportedOnDeviceLanguages) -> PackState.AVAILABLE
                        else -> PackState.UNSUPPORTED
                    }
                )
                engine.destroy()
            }

            override fun onError(error: Int) { onResult(PackState.UNKNOWN); engine.destroy() }
        })
    }

    /** Просит систему скачать пакет распознавания языка `tag` (Android 13+); ход загрузки показывает сама система. На старых Android открывает настройки голосового ввода. */
    fun download(context: Context, tag: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && SpeechRecognizer.isOnDeviceRecognitionAvailable(context)) downloadOnDevice(context, tag)
        else openRecognitionSettings(context)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun downloadOnDevice(context: Context, tag: String) {
        val engine = SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
        runCatching { engine.triggerModelDownload(intentFor(tag)) }
        Handler(Looper.getMainLooper()).postDelayed({ engine.destroy() }, DESTROY_DELAY_MS)   // загрузку ведёт система, но закрывать клиента сразу не стоит
    }

    private const val TTS_SETTINGS = "com.android.settings.TTS_SETTINGS"
    private const val DESTROY_DELAY_MS = 3000L
}
