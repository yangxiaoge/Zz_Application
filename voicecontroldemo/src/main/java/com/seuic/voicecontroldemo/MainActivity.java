package com.seuic.voicecontroldemo;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.seuic.voicecontroldemo.speech_sms.SpeechService;
import com.seuic.voicecontroldemo.util.CheckUtil;
import com.seuic.voicecontroldemo.voicerecognition.VoiceService;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //申请权限
        requestPermission(Manifest.permission.READ_CONTACTS,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CALL_PHONE,
                Manifest.permission.SEND_SMS,
                Manifest.permission.READ_SMS,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        //-------------------默认格式转换-----------------------------
        /*String[] pyStrs = PinyinHelper.toHanyuPinyinStringArray('重');

        for (String s : pyStrs) {
            Log.i("pinyin",s);
        }*/
    }

    /**
     * 申请权限
     */
    private void requestPermission(String... permissions) {
        AndPermission.with(this)
                .runtime()
                .permission(permissions)
                .onGranted(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> permissions) {
                        //SpeechService是否在运行中
                        if (!CheckUtil.isServiceWorked(MainActivity.this, "com.seuic.voicecontroldemo.speech_sms.SpeechService")) {
                            startService(new Intent(getBaseContext(), SpeechService.class));
                        }

                        //VoiceService是否在运行中
                        if (!CheckUtil.isServiceWorked(MainActivity.this, "com.seuic.voicecontroldemo.voicerecognition.VoiceService")) {
                            startService(new Intent(getBaseContext(), VoiceService.class));
                        }
                    }
                })
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(@NonNull List<String> permissions) {
                        Log.e("权限被拒绝", "权限被拒绝permissions = " + permissions.get(0));
                        if (permissions.size() == 1 && permissions.contains(Manifest.permission.RECORD_AUDIO)) {
                            Toast.makeText(MainActivity.this, "没有录音权限或者麦克风被其他应用占用", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(MainActivity.this, "权限被拒绝,需要相应的权限才能启用", Toast.LENGTH_SHORT).show();
                        }
                        //杀掉服务
                        //SpeechService是否在运行中
                        if (CheckUtil.isServiceWorked(MainActivity.this, "com.seuic.voicecontroldemo.speech_sms.SpeechService")) {
                            stopService(new Intent(getBaseContext(), SpeechService.class));
                        }
                        //VoiceService是否在运行中
                        if (CheckUtil.isServiceWorked(MainActivity.this, "com.seuic.voicecontroldemo.voicerecognition.VoiceService")) {
                            stopService(new Intent(getBaseContext(), VoiceService.class));
                        }
                        //关闭页面
                        finish();
                    }
                })
                .start();
    }
}
