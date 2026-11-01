package com.example.lingocards.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public final class GetDb {
    public static SQLiteDatabase run(Context ctx) {
        synchronized (DbHolder.class) {
            if (DbHolder.instance == null) {
                Context app = ctx.getApplicationContext();
                app.deleteDatabase(DbConfig.LEGACY_NAME);
                DbHolder.instance = new DbHelper(app);
            }
            return DbHolder.instance.getWritableDatabase();
        }
    }
}
