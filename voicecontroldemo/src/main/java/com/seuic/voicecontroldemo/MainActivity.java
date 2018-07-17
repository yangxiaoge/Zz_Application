package com.seuic.voicecontroldemo;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.seuic.voicecontroldemo.speech_sms.SpeechService;
import com.seuic.voicecontroldemo.voicerecognition.VoiceService;
import com.tbruyelle.rxpermissions2.RxPermissions;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class MainActivity extends AppCompatActivity {
    private RxPermissions rxPermissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rxPermissions = new RxPermissions(this);

        //申请权限
        rxPermissions.request(Manifest.permission.READ_CONTACTS, Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_PHONE_STATE, Manifest.permission.SEND_SMS, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        if (!aBoolean) {
                            Toast.makeText(MainActivity.this, "权限被拒绝,需要相应的权限才能启用", Toast.LENGTH_SHORT).show();

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
                        } else {
                            //SpeechService是否在运行中
                            if (!CheckUtil.isServiceWorked(MainActivity.this, "com.seuic.voicecontroldemo.speech_sms.SpeechService")) {
                                startService(new Intent(getBaseContext(), SpeechService.class));
                            }

                            //VoiceService是否在运行中
                            if (!CheckUtil.isServiceWorked(MainActivity.this, "com.seuic.voicecontroldemo.voicerecognition.VoiceService")) {
                                startService(new Intent(getBaseContext(), VoiceService.class));
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }

}
