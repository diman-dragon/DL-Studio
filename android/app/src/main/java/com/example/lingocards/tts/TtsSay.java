package com.example.lingocards.tts;

import android.speech.tts.TextToSpeech;
import com.getcapacitor.PluginCall;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

public final class TtsSay {
    private static final AtomicInteger SEQ = new AtomicInteger();

    /**
     * Сначала язык, скорость и высота; потом речь. waitCall — вызов JS, который надо завершить,
     * когда реплика дозвучит (диалоги); null — не ждать.
     */
    public static void run(String text, String lang, float rate, float pitch, PluginCall waitCall) throws Exception {
        TtsReady.run();
        int r = TtsHolder.engine.setLanguage(Locale.forLanguageTag(lang));
        if (r == TextToSpeech.LANG_MISSING_DATA || r == TextToSpeech.LANG_NOT_SUPPORTED)
            throw new Exception("Голос для языка недоступен: " + lang);
        TtsHolder.engine.setSpeechRate(Math.max(0.3f, Math.min(rate, 2.5f)));
        TtsHolder.engine.setPitch(Math.max(0.5f, Math.min(pitch, 2f)));
        TtsHolder.engine.setOnUtteranceProgressListener(TtsProgress.INSTANCE);
        String id = "fc" + SEQ.incrementAndGet();
        if (waitCall != null) TtsHolder.pending.put(id, waitCall);
        if (TtsHolder.engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, id) == TextToSpeech.ERROR) {
            TtsHolder.pending.remove(id);
            throw new Exception("Не удалось озвучить текст");
        }
    }
}
