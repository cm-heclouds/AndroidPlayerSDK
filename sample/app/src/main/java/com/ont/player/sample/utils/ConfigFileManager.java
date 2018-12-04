package com.ont.player.sample.utils;

import android.os.Environment;

import java.io.File;


public class ConfigFileManager {
    public static String getUserFilesDir() {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "oneNet" + File.separator;
        File pathFile = new File(path);
        if(!pathFile.exists())
            pathFile.mkdirs();
        return path;
    }
}
