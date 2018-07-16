package com.seuic.voicecontroldemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.seuic.voicecontroldemo.speech_sms.SpeechService;

public class MyBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {

            //SpeechService是否在运行中
            if (!CheckUtil.isServiceWorked(context, "com.seuic.voicecontroldemo.speech_sms.SpeechService")) {
                /* 服务开机自启动 */
                Intent service = new Intent(context, SpeechService.class);
                context.startService(service);
            }
        }
    }
}
