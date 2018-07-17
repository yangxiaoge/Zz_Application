package com.seuic.voicecontroldemo;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.seuic.voicecontroldemo.speech_sms.SpeechService;
import com.seuic.voicecontroldemo.voicerecognition.VoiceService;

import java.util.List;

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
    }  private void openApp1(String packageName) {
        try {
            PackageInfo pi = getPackageManager().getPackageInfo(packageName, 0);

            Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
            resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            resolveIntent.setPackage(pi.packageName);

            List<ResolveInfo> apps = getPackageManager().queryIntentActivities(resolveIntent, 0);

            ResolveInfo ri = apps.iterator().next();
            if (ri != null ) {
                String packageName1 = ri.activityInfo.packageName;
                String className = ri.activityInfo.name;

                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);

                ComponentName cn = new ComponentName(packageName1, className);

                intent.setComponent(cn);
                startActivity(intent);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
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
