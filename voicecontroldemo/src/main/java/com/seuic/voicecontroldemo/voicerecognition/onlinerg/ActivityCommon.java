package com.seuic.voicecontroldemo.voicerecognition.onlinerg;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.seuic.voicecontroldemo.R;
import com.seuic.voicecontroldemo.speech_sms.SpeechService;
import com.seuic.voicecontroldemo.util.CheckUtil;
import com.seuic.voicecontroldemo.voicerecognition.util.Logger;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;

import java.util.List;

/**
 * Created by fujiayi on 2017/6/20.
 */

public abstract class ActivityCommon extends AppCompatActivity {
    protected TextView txtLog;
    protected Button btn;
    protected Button setting;
    protected TextView txtResult;

    protected Handler handler;

    protected String descText;

    protected int layout = R.layout.common;

    protected Class settingActivityClass = null;

    protected boolean running = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setStrictMode();
        //InFileStream.setContext(this);
        setContentView(layout);
        initView();
        handler = new Handler() {

            /*
             * @param msg
             */
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                handleMsg(msg);
            }

        };
        Logger.setHandler(handler);
        initPermission();
        initRecog();
    }


    protected abstract void initRecog();

    protected void handleMsg(Message msg) {
        if (txtLog != null && msg.obj != null) {
            txtLog.append(msg.obj.toString() + "\n");
        }
    }

    protected void initView() {
        txtResult = (TextView) findViewById(R.id.txtResult);
        txtLog = (TextView) findViewById(R.id.txtLog);
        btn = (Button) findViewById(R.id.btn);
        setting = (Button) findViewById(R.id.setting);
        txtLog.setText(descText + "\n");
        if (setting != null && settingActivityClass != null) {
            setting.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    running = true;
                    Intent intent = new Intent(ActivityCommon.this, settingActivityClass);
                    startActivityForResult(intent, 1);
                }
            });
        }

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
                        if (!CheckUtil.isServiceWorked(ActivityCommon.this, "com.seuic.voicecontroldemo.speech_sms.SpeechService")) {
                            startService(new Intent(getBaseContext(), SpeechService.class));
                        }
                    }
                })
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(@NonNull List<String> permissions) {
                        Log.e("权限被拒绝", "权限被拒绝permissions = " + permissions.get(0));
                        if (permissions.size() == 1 && permissions.contains(Manifest.permission.RECORD_AUDIO)) {
                            Toast.makeText(ActivityCommon.this, "没有录音权限或者麦克风被其他应用占用", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(ActivityCommon.this, "权限被拒绝,需要相应的权限才能启用", Toast.LENGTH_SHORT).show();
                        }
                        //杀掉服务
                        //SpeechService是否在运行中
                        if (CheckUtil.isServiceWorked(ActivityCommon.this, "com.seuic.voicecontroldemo.speech_sms.SpeechService")) {
                            stopService(new Intent(getBaseContext(), SpeechService.class));
                        }
                        //关闭页面
                        finish();
                    }
                })
                .start();
    }

    /*@Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // 此处为android 6.0以上动态授权的回调，用户自行实现。
    }*/

    /**
     * android 6.0 以上需要动态申请权限
     */
    private void initPermission() {
        String[] permissions = {
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        /*ArrayList<String> toApplyList = new ArrayList<String>();

        for (String perm : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                toApplyList.add(perm);
                // 进入到这里代表没有权限.

            }
        }
        String[] tmpList = new String[toApplyList.size()];
        if (!toApplyList.isEmpty()) {
            ActivityCompat.requestPermissions(this, toApplyList.toArray(tmpList), 123);
        }*/

    }

    private void setStrictMode() {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .penaltyDeath()
                .build());

    }
}
