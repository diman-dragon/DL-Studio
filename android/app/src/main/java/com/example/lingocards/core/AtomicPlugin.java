package com.example.lingocards.core;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** База «один плагин — одна операция»: единственный метод exec, единая обработка ошибок. */
public abstract class AtomicPlugin extends Plugin {
    private static final ExecutorService BACKGROUND = Executors.newSingleThreadExecutor();
    /** Вернуть результат; null — ответ придёт позже (activity result). */
    protected abstract JSObject handle(PluginCall call) throws Exception;

    /** Тяжёлые операции могут явно выполняться вне UI-потока. */
    protected boolean runInBackground() { return false; }

    @PluginMethod
    public final void exec(PluginCall call) {
        if (call == null) return;
        if (runInBackground()) BACKGROUND.execute(() -> settle(call, () -> handle(call)));
        else settle(call, () -> handle(call));
    }

    protected void settle(PluginCall call, Task task) {
        if (call == null) return;
        try {
            JSObject r = task.run();
            if (r != null) call.resolve(r);
        } catch (Exception e) {
            call.reject(String.valueOf(e.getMessage()), e);
        }
    }
}
