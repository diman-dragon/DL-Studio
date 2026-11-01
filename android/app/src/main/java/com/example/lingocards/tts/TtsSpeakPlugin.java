package com.example.lingocards.tts;

import com.example.lingocards.core.AtomicPlugin;
import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;
import com.getcapacitor.annotation.CapacitorPlugin;

/** Озвучка. wait: true — ответ придёт, когда реплика дозвучит ({ interrupted }); иначе сразу после запуска речи. */
@CapacitorPlugin(name = "TtsSpeak")
public class TtsSpeakPlugin extends AtomicPlugin {
    @Override public void load() { TtsInit.run(getContext()); }

    @Override protected JSObject handle(PluginCall c) throws Exception {
        boolean wait = Boolean.TRUE.equals(c.getBoolean("wait", false));
        TtsSay.run(c.getString("text", ""), c.getString("lang", "en-US"),
            c.getFloat("rate", 1f), c.getFloat("pitch", 1f), wait ? c : null);
        return wait ? null : new JSObject();
    }
}
