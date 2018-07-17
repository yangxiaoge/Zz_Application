package com.seuic.voicecontroldemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.seuic.voicecontroldemo.speech_sms.SpeechService;
import com.seuic.voicecontroldemo.voicerecognition.VoiceService;

public class MyBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())
                || "android.media.AUDIO_BECOMING_NOISY".equals(intent.getAction())) {
            Log.i("启动", "开机启动了，啊啊啊啊啊啊啊啊啊啊");

            //SharedPreferences sp = context.getSharedPreferences("User", Context.MODE_PRIVATE);

            //SpeechService是否在运行中
            if (!CheckUtil.isServiceWorked(context, "com.seuic.voicecontroldemo.speech_sms.SpeechService")) {
                /* 服务开机自启动 */
                Intent service = new Intent(context, SpeechService.class);
                context.startService(service);
            }
            //VoiceService是否在运行中
            if (!CheckUtil.isServiceWorked(context, "com.seuic.voicecontroldemo.voicerecognition.VoiceService")) {
                context.startService(new Intent(context, VoiceService.class));
            }
        }
    }
}
