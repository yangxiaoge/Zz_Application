package com.seuic.voicecontroldemo.voicerecognition;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.baidu.speech.asr.SpeechConstant;
import com.seuic.voicecontroldemo.voicerecognition.control.MyRecognizer;
import com.seuic.voicecontroldemo.voicerecognition.control.MyWakeup;
import com.seuic.voicecontroldemo.voicerecognition.rg.IStatus;
import com.seuic.voicecontroldemo.voicerecognition.rg.MessageStatusRecogListener;
import com.seuic.voicecontroldemo.voicerecognition.rg.StatusRecogListener;
import com.seuic.voicecontroldemo.voicerecognition.wakeup.IWakeupListener;
import com.seuic.voicecontroldemo.voicerecognition.wakeup.SimpleWakeupListener;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by yangjianan on 2018/7/16.
 */
public class VoiceService extends Service implements IStatus {
    private static final String TAG = VoiceService.class.getSimpleName();
    protected MyWakeup myWakeup;
    /**
     * 识别控制器，使用MyRecognizer控制识别的流程
     */
    protected MyRecognizer myRecognizer;
    /**
     * 0: 方案1， 唤醒词说完后，直接接句子，中间没有停顿。
     * >0 : 方案2： 唤醒词说完后，中间有停顿，然后接句子。推荐4个字 1500ms
     * <p>
     * backTrackInMs 最大 15000，即15s
     */
    private int backTrackInMs = 1500;

    @SuppressLint("HandlerLeak")
    private Handler recogHandler = new Handler() {
        /*
         * @param msg
         */
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            handleMsg(msg);
        }

    };

    protected void handleMsg(Message msg) {
        if (msg.what == STATUS_WAKEUP_SUCCESS) {
            // 此处 开始正常识别流程
            Map<String, Object> params = new LinkedHashMap<String, Object>();
            params.put(SpeechConstant.ACCEPT_AUDIO_VOLUME, false);
            params.put(SpeechConstant.VAD, SpeechConstant.VAD_DNN);
            // 如识别短句，不需要需要逗号，使用1536搜索模型。其它PID参数请看文档
            params.put(SpeechConstant.PID, 1536);
            if (backTrackInMs > 0) { // 方案1， 唤醒词说完后，直接接句子，中间没有停顿。
                params.put(SpeechConstant.AUDIO_MILLS, System.currentTimeMillis() - backTrackInMs);

            }
            myRecognizer.cancel();
            myRecognizer.start(params);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initRecog(); // 初始化识别引擎
        return START_STICKY;
    }


    // 初始化识别引擎
    private void initRecog() {
        /*IWakeupListener listener = new SimpleWakeupListener();
        // 改为 SimpleWakeupListener 后，不依赖handler，但将不会在UI界面上显示
        myWakeup = new MyWakeup(this, listener);

        StatusRecogListener recogListener =  new MessageStatusRecogListener(recogHandler);
        // 改为 SimpleWakeupListener 后，不依赖handler，但将不会在UI界面上显示
        myRecognizer = new MyRecognizer(this, recogListener);

        IWakeupListener listener = new RecogWakeupListener(handler);
        myWakeup = new MyWakeup(this, listener);*/

        if (myRecognizer != null) return; //防止多实例
        StatusRecogListener recogListener = new MessageStatusRecogListener(recogHandler);
        myRecognizer = new MyRecognizer(this, recogListener);
        if (myWakeup != null) return;//防止多实例
        IWakeupListener listener = new SimpleWakeupListener();
        myWakeup = new MyWakeup(this, listener);

        /*IWakeupListener listener = new SimpleWakeupListener();
        // 改为 SimpleWakeupListener 后，不依赖handler，但将不会在UI界面上显示
        myWakeup = new MyWakeup(this, listener);*/

        //启动唤醒
        //start();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                start();
            }
        },1000);
    }

    //启动唤醒
    private void start() {
        Map<String, Object> params = new HashMap<>();
        params.put(SpeechConstant.WP_WORDS_FILE, "assets:///WakeUp.bin");
        // "assets:///WakeUp.bin" 表示WakeUp.bin文件定义在assets目录下

        // params.put(SpeechConstant.ACCEPT_AUDIO_DATA,true);
        // params.put(SpeechConstant.ACCEPT_AUDIO_VOLUME,true);
        // params.put(SpeechConstant.IN_FILE,"res:///com/baidu/android/voicedemo/wakeup.pcm");
        // params里 "assets:///WakeUp.bin" 表示WakeUp.bin文件定义在assets目录下
        myWakeup.start(params);
    }

    //停止唤醒
    private void stop() {
        myWakeup.stop();
    }

    @Override
    public void onDestroy() {
        myWakeup.release();
        myRecognizer.release();
        Log.i(TAG, "释放资源成功");
        super.onDestroy();
    }
}
