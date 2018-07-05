package com.yjn.alisophix;

import android.app.Application;
import android.content.Context;

import com.taobao.sophix.SophixManager;

/**
 * Created by yangjianan on 2018/7/5.
 */
public class MyRealApplication extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // queryAndLoadNewPatch不可放在attachBaseContext 中，否则无网络权限，建议放在后面任意时刻，如onCreate中
        SophixManager.getInstance().queryAndLoadNewPatch();
    }
}
