package com.dlstudio.app.study

import android.speech.SpeechRecognizer

/** Один способ распознавания речи; `offline` — просить локальную модель (для остальных разрешён и интернет). */
class RecognizerEngine(val offline: Boolean, val create: () -> SpeechRecognizer)
