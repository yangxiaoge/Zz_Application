package com.seuic.voicecontroldemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.seuic.voicecontroldemo.speech_sms.SpeechService;
import com.seuic.voicecontroldemo.voicerecognition.VoiceService;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //SpeechService是否在运行中
        if (!CheckUtil.isServiceWorked(this, "com.seuic.voicecontroldemo.speech_sms.SpeechService")) {
            startService(new Intent(getBaseContext(), SpeechService.class));
        }

        //VoiceService是否在运行中
        if (!CheckUtil.isServiceWorked(this, "com.seuic.voicecontroldemo.voicerecognition.VoiceService")) {
            startService(new Intent(getBaseContext(), VoiceService.class));
        }
    }

    @Override
    protected void onDestroy() {
        //不要杀服务，需要持续后台运行
        /*//SpeechService是否在运行中
        if (CheckUtil.isServiceWorked(this, "com.seuic.voicecontroldemo.speech_sms.SpeechService")) {
            stopService(new Intent(getBaseContext(), SpeechService.class));
        }
        //VoiceService是否在运行中
        if (CheckUtil.isServiceWorked(this, "com.seuic.voicecontroldemo.voicerecognition.VoiceService")) {
            stopService(new Intent(getBaseContext(), VoiceService.class));
        }*/
        super.onDestroy();
    }
}
