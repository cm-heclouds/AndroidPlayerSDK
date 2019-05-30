package com.ont.player.sample;

import android.app.Application;

import com.ont.player.sample.def.IRequestDef;
import com.ont.player.sample.utils.PropertyUtils;

import java.util.Properties;

/**
 * Created by betali on 2018/1/18.
 */

public class BaseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // init request url
        Properties properties = PropertyUtils.getProperties(this);
        IRequestDef.IRequestUrlDef.API_URL = properties.getProperty("request.api.url");
    }
}
