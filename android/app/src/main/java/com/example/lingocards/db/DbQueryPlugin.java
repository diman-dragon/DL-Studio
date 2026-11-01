package com.example.lingocards.db;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "DbQuery")
public class DbQueryPlugin extends DbPlugin {
    @Override protected JSObject handle(PluginCall c) throws Exception {
        JSArray rows = QueryRows.run(GetDb.run(getContext()), c.getString("sql"), c.getArray("params", new JSArray()));
        return new JSObject().put("rows", rows);
    }
}
