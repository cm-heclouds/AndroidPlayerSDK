package com.ont.player.sample.utils;

import android.content.Context;

import com.ont.player.sample.R;

import java.util.Properties;

/**
 * Created by betali on 2019/3/28.
 */
public class PropertyUtils {

    public static Properties getProperties(Context context){

        try {
            Properties properties = new Properties();
            properties.load(context.getResources().openRawResource(R.raw.config));
            return properties;
        } catch (Exception e) {
        }
        return null;
    }
}
