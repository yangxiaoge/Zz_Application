package com.seuic.voicecontroldemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.seuic.voicecontroldemo.speech_sms.SpeechService;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //SpeechService是否在运行中
        if (!CheckUtil.isServiceWorked(this,"com.seuic.voicecontroldemo.speech_sms.SpeechService")){
            startService(new Intent(getBaseContext(),SpeechService.class));
        }

    }

    @Override
    protected void onDestroy() {
        if (CheckUtil.isServiceWorked(this,"com.seuic.voicecontroldemo.speech_sms.SpeechService")){
            stopService(new Intent(getBaseContext(),SpeechService.class));
        }
        super.onDestroy();
    }
}
