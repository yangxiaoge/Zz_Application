package com.seuic.voicecontroldemo;

import android.app.Application;

import com.seuic.voicecontroldemo.util.CrashHandler;

/**
 * Created by yangjianan on 2018/7/17.
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler.getInstance().init(this);
    }
}
