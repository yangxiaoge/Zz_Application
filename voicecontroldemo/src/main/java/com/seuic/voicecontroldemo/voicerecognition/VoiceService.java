package com.seuic.voicecontroldemo.voicerecognition;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by yangjianan on 2018/7/16.
 */
public class VoiceService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
