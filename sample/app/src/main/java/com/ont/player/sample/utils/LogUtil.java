package com.ont.player.sample.utils;

import android.util.Log;

/**
 * Created by cy8166 on 2018/2/28.
 */

public class LogUtil {
    private static boolean bOpen = true;

    public static void i(String tag, String content){
        if(bOpen)
            Log.i(tag, content);
    }

    public static void e(String tag, String content){
        if(bOpen)
            Log.e(tag, content);
    }

    public static void d(String tag, String content){
        if(bOpen)
            Log.d(tag, content);
    }
}
