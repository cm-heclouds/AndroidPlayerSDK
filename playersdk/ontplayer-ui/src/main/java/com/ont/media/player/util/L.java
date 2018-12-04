package com.ont.media.player.util;

import android.util.Log;

/**
 * 日志类
 * Created by Devlin_n on 2017/6/5.
 */

public class L {

    private static final String TAG = "DKPlayer";
    private static boolean isDebug = false;

    public static void i(String msg) {
        if (isDebug)
            Log.i(TAG, msg);
    }

    public static void i(String tag, String msg) {
        if (isDebug)
            Log.i(tag, msg);
    }

    public static void d(String msg) {
        if (isDebug) {
            Log.d(TAG, msg);
        }
    }

    public static void d(String tag, String msg) {
        if (isDebug)
            Log.d(tag, msg);
    }

    public static void e(String msg) {
        if (isDebug) {
            Log.e(TAG, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (isDebug)
            Log.e(tag, msg);
    }

    public static void setDebug(boolean isDebug) {

        L.isDebug = isDebug;
    }
}
