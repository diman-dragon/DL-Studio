package com.example.lingocards.db;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "DbBatch")
public class DbBatchPlugin extends DbPlugin {
    @Override protected JSObject handle(PluginCall c) throws Exception {
        RunBatch.run(GetDb.run(getContext()), c.getArray("statements", new JSArray()));
        return new JSObject();
    }
}
