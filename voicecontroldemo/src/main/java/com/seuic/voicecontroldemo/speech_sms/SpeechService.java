package com.seuic.voicecontroldemo.speech_sms;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.speech.EventManager;
import com.baidu.tts.chainofresponsibility.logger.LoggerProxy;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;
import com.seuic.voicecontroldemo.speech_sms.control.InitConfig;
import com.seuic.voicecontroldemo.speech_sms.control.MySyntherizer;
import com.seuic.voicecontroldemo.speech_sms.control.NonBlockSyntherizer;
import com.seuic.voicecontroldemo.speech_sms.listener.UiMessageListener;
import com.seuic.voicecontroldemo.speech_sms.util.MainHandlerConstant;
import com.seuic.voicecontroldemo.speech_sms.util.OfflineResource;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yangjianan on 2018/7/16.
 * 语音合成
 */
public class SpeechService extends Service implements MainHandlerConstant, SmsObserver.SmsSpeakCallback,
        CustomPhoneStateListener.IncomingPhoneCallback {
    private static final String TAG = SpeechService.class.getSimpleName();
    // ================== 初始化参数设置开始 ==========================
    /**
     * 发布时请替换成自己申请的appId appKey 和 secretKey。注意如果需要离线合成功能,请在您申请的应用中填写包名。
     * 本demo的包名是com.baidu.tts.sample，定义在build.gradle中。
     */
    protected String appId = "11539617";

    protected String appKey = "mHxxUPKhhympAYGIZvGeB1FM";

    protected String secretKey = "PV15bNSUiNqdj4fjRU3Zsueb4XBq9brL";

    // TtsMode.MIX; 离在线融合，在线优先； TtsMode.ONLINE 纯在线； 没有纯离线
    protected TtsMode ttsMode = TtsMode.MIX;

    // 离线发音选择，VOICE_FEMALE即为离线女声发音。
    // assets目录下bd_etts_common_speech_m15_mand_eng_high_am-mix_v3.0.0_20170505.dat为离线男声模型；
    // assets目录下bd_etts_common_speech_f7_mand_eng_high_am-mix_v3.0.0_20170512.dat为离线女声模型
    protected String offlineVoice = OfflineResource.VOICE_FEMALE;

    // ===============初始化参数设置完毕，更多合成参数请至getParams()方法中设置 =================

    // 主控制类，所有合成控制方法从这个类开始
    protected MySyntherizer synthesizer;


    @SuppressLint("HandlerLeak")
    private Handler speechHandler = new Handler() {
        /*
         * @param msg
         */
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            handle(msg);
        }

    };

    @SuppressLint("HandlerLeak")
    public Handler smsHandler = new Handler() {
        //这里可以进行回调的操作
        //TODO
    };
    private EventManager wp; // this是Activity或其它

    private void handle(Message msg) {
        int what = msg.what;
        switch (what) {
            case INIT_SUCCESS:
                //TODO 初始化成功了
                smsObserver = new SmsObserver(this, smsHandler, this);
                getContentResolver().registerContentObserver(Uri.parse("content://sms"), true, smsObserver);
                Log.i(TAG, "初始化成功了，ok");
                break;
            case PRINT:
                String message = (String) msg.obj;
                Log.i(TAG, message);
                break;
            case UI_CHANGE_INPUT_TEXT_SELECTION:
                break;
            case UI_CHANGE_SYNTHES_TEXT_SELECTION:
                break;
            default:
                break;
        }
    }

    private SmsObserver smsObserver;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initialTts(); // 初始化TTS引擎
        registerPhoneStateListener();
        return START_STICKY;
    }

    /**
     * 初始化引擎，需要的参数均在InitConfig类里
     * <p>
     * DEMO中提供了3个SpeechSynthesizerListener的实现
     * MessageListener 仅仅用log.i记录日志，在logcat中可以看见
     * UiMessageListener 在MessageListener的基础上，对handler发送消息，实现UI的文字更新
     * FileSaveListener 在UiMessageListener的基础上，使用 onSynthesizeDataArrived回调，获取音频流
     */
    protected void initialTts() {
        LoggerProxy.printable(true); // 日志打印在logcat中
        // 设置初始化参数
        // 此处可以改为 含有您业务逻辑的SpeechSynthesizerListener的实现类
        SpeechSynthesizerListener listener = new UiMessageListener(speechHandler);

        Map<String, String> params = getParams();


        // appId appKey secretKey 网站上您申请的应用获取。注意使用离线合成功能的话，需要应用中填写您app的包名。包名在build.gradle中获取。
        InitConfig initConfig = new InitConfig(appId, appKey, secretKey, ttsMode, params, listener);

        if (synthesizer != null) return;
        synthesizer = new NonBlockSyntherizer(this, initConfig, speechHandler); // 此处可以改为MySyntherizer 了解调用过程
    }

    /**
     * 合成的参数，可以初始化时填写，也可以在合成前设置。
     *
     * @return
     */
    protected Map<String, String> getParams() {
        Map<String, String> params = new HashMap<String, String>();
        // 以下参数均为选填
        // 设置在线发声音人： 0 普通女声（默认） 1 普通男声 2 特别男声 3 情感男声<度逍遥> 4 情感儿童声<度丫丫>
        params.put(SpeechSynthesizer.PARAM_SPEAKER, "0");
        // 设置合成的音量，0-9 ，默认 5
        params.put(SpeechSynthesizer.PARAM_VOLUME, "9");
        // 设置合成的语速，0-9 ，默认 5
        params.put(SpeechSynthesizer.PARAM_SPEED, "5");
        // 设置合成的语调，0-9 ，默认 5
        params.put(SpeechSynthesizer.PARAM_PITCH, "5");

        params.put(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_DEFAULT);
        // 该参数设置为TtsMode.MIX生效。即纯在线模式不生效。
        // MIX_MODE_DEFAULT 默认 ，wifi状态下使用在线，非wifi离线。在线状态下，请求超时6s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI wifi状态下使用在线，非wifi离线。在线状态下， 请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_NETWORK ， 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE, 2G 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线

        // 离线资源文件， 从assets目录中复制到临时目录，需要在initTTs方法前完成
        OfflineResource offlineResource = createOfflineResource(offlineVoice);
        // 声学模型文件路径 (离线引擎使用), 请确认下面两个文件存在
        params.put(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, offlineResource.getTextFilename());
        params.put(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE,
                offlineResource.getModelFilename());
        return params;
    }

    protected OfflineResource createOfflineResource(String voiceType) {
        OfflineResource offlineResource = null;
        try {
            offlineResource = new OfflineResource(this, voiceType);
        } catch (IOException e) {
            // IO 错误自行处理
            e.printStackTrace();
            Log.i(TAG, "【error】:copy files from assets failed." + e.getMessage());
        }
        return offlineResource;
    }

    /**
     * 短信内容回调
     *
     * @param text sms text
     */
    @Override
    public void smsText(String text) {
        speak(text);
    }

    /**
     * 来电电话回调
     *
     * @param phoneNumber phone
     */
    @Override
    public void phoneNumber(String phoneNumber) {
        speak(phoneNumber + "给您来电啦");
    }

    /**
     * speak 实际上是调用 synthesize后，获取音频流，然后播放。
     * 获取音频流的方式见SaveFileActivity及FileSaveListener
     * 需要合成的文本text的长度不能超过1024个GBK字节。
     */
    private synchronized void speak(String text) {
        // 需要合成的文本text的长度不能超过1024个GBK字节。
        if (TextUtils.isEmpty(text)) {
            return;
        }
        // 合成前可以修改参数：
        // Map<String, String> params = getParams();
        // synthesizer.setParams(params);
        int result = synthesizer.speak(text);
        checkResult(result, "speak");
    }

    private void checkResult(int result, String method) {
        if (result != 0) {
            Log.i(TAG, "error code :" + result + " method:" + method + ", 错误码文档:http://yuyin.baidu.com/docs/tts/122 ");
        }
    }

    private void registerPhoneStateListener() {
        CustomPhoneStateListener customPhoneStateListener = new CustomPhoneStateListener(this, this);
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            telephonyManager.listen(customPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }
    }

    @Override
    public void onDestroy() {
        getContentResolver().unregisterContentObserver(smsObserver);
        synthesizer.release();
        Log.i(TAG, "释放资源成功");
        super.onDestroy();
    }
}
