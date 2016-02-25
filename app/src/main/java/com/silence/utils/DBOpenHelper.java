package com.silence.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;

/**
 * Created by Silence on 2016/1/30 0030.
 */
public class DBOpenHelper {
    private static DBOpenHelper sDBOpenHelper;
    private Context mContext;

    private DBOpenHelper(Context context) {
        mContext = context;
    }

    public static DBOpenHelper getInstance(Context context) {
        if (sDBOpenHelper == null) {
            sDBOpenHelper = new DBOpenHelper(context);
        }
        return sDBOpenHelper;
    }

    public SQLiteDatabase getDatabase() {
        String path = mContext.getDir(Const.DB_DIR, Context.MODE_PRIVATE) + File.separator + Const.DB_NAME;
        return SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READWRITE);
    }
}