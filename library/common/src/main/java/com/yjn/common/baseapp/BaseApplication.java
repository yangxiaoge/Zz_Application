package com.yjn.common.baseapp;

import android.content.Context;
import android.content.res.Resources;
import android.support.multidex.MultiDexApplication;

/**
 * Created by yang.jinan on 2017/9/3.
 */

public class BaseApplication extends MultiDexApplication {
    private static BaseApplication baseApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        baseApplication = this;
    }

    public static BaseApplication getInstance() {
        return baseApplication;
    }

    public static Context getAppContext() {
        return baseApplication.getApplicationContext();
    }

    public static Resources getAppResources() {
        return baseApplication.getResources();
    }
}
