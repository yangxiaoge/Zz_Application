package com.seuic.voicecontroldemo.voicerecognition;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.baidu.speech.asr.SpeechConstant;
import com.seuic.voicecontroldemo.util.PhoneUtils;
import com.seuic.voicecontroldemo.voicerecognition.control.MyRecognizer;
import com.seuic.voicecontroldemo.voicerecognition.control.MyWakeup;
import com.seuic.voicecontroldemo.voicerecognition.rg.IStatus;
import com.seuic.voicecontroldemo.voicerecognition.rg.MessageStatusRecogListener;
import com.seuic.voicecontroldemo.voicerecognition.rg.StatusRecogListener;
import com.seuic.voicecontroldemo.voicerecognition.util.Logger;
import com.seuic.voicecontroldemo.voicerecognition.wakeup.IWakeupListener;
import com.seuic.voicecontroldemo.voicerecognition.wakeup.RecogWakeupListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        if (msg.what == STATUS_FINISHED) {
            Log.i(TAG, "finished ----- ");
            if (msg.obj != null) {
                Log.i(TAG, msg.obj.toString() + "\n");

                String str = msg.obj.toString().trim();
                if (str.contains("识别结束结果是")) {
                    //取出语音信息
                    String res = str.split(":")[1];
                    if (TextUtils.isEmpty(res)) return;
                    //测试直接发短信
                    // sendMessage("13804714681", "测试7.1直接发送短信");

                    // 关键词 "打开微信",供测试用
                    if (res.contains("打开微信")) {
                        Intent intent = getPackageManager().getLaunchIntentForPackage("com.tencent.mm");
                        if (intent == null) {
                            Toast.makeText(this, "您没有安装微信", Toast.LENGTH_LONG).show();
                        } else {
                            startActivity(intent);
                            Toast.makeText(this, "已为您打开微信", Toast.LENGTH_LONG).show();
                        }
                        return;
                    }

                    // 关键词 "打开"
                    if (res.contains("打开") && res.contains("地图")) {
                        String mapName = res.substring(res.indexOf("打开") + 2, res.indexOf("地图") + 2);
                        String destName = res.substring(res.indexOf("去") + 1);
                        openMap(mapName, destName);
                        return;
                    }

                    // 关键词 "打电话"
                    if (res.contains("打电话")) {
                        if (!res.contains("给")) return;
                        String phoneOrName = null;
                        if (res.indexOf("给") > res.indexOf("打电话")) { //打电话给张三
                            phoneOrName = res.substring(res.indexOf("打电话给") + 4);
                        } else if (res.indexOf("打电话") > res.indexOf("给")) { //给张三打电话
                            phoneOrName = res.substring(res.indexOf("给") + 1, res.indexOf("打电话"));
                        }
                        Log.i(TAG, "打电话 " + phoneOrName);
                        call(phoneOrName);
                        return;
                    }

                    //给XXX回复短信，短信内容是XXXX。
                    if (res.contains("回复") && res.contains("短信") && res.contains("短信内容是")) {
                        String name = res.substring(res.indexOf("给") + 1, res.indexOf("回复短信"));
                        String content = res.substring(res.indexOf("短信内容是") + 5);
                        doSendSMSTo(name, content);
                        return;
                    }
                    //发短信给XXX，短信内容是XXXX (给XXX发短信，短信内容是XXXX)
                    if (res.contains("发短信") && res.contains("给") && res.contains("短信内容是")) {
                        String name = null;
                        if ((res.indexOf("给") > res.indexOf("发短信"))) {
                            name = res.substring(res.indexOf("给") + 1, res.indexOf("短信内容是"));
                        } else if (res.indexOf("发短信") > res.indexOf("给")) {
                            name = res.substring(res.indexOf("给") + 1, res.indexOf("发短信"));
                        }
                        String content = res.substring(res.indexOf("短信内容是") + 5);
                        doSendSMSTo(name, content);
                        return;
                    }

                    // 关键词 "挂断"
                    if (res.contains("挂断") || res.contains("拒接")) {

                        PhoneUtils.endCall(this);
                        /*boolean endCallSuccess = HangUpTelephonyUtil.endCall(this);
                        if (!endCallSuccess) {
                            HangUpTelephonyUtil.killCall(this);
                        }*/
                        return;
                    }
                }
            }
        }

    }

    // 打开地图
    private void openMap(String appName, String destnation) {
        PackageManager packageManager = getPackageManager();
        // 获取手机里的应用列表
        List<PackageInfo> pInfo = packageManager.getInstalledPackages(0);
        for (int i = 0; i < pInfo.size(); i++) {
            PackageInfo p = pInfo.get(i);
            // 获取相关包的 <application> 中的 label 信息，也就是 --> 应用程序的名字
            String label = packageManager.getApplicationLabel(p.applicationInfo).toString();
            //Log.d("tag","label = " + label);
            if (label.contains(appName)) { // 比较 label
                String pName = p.packageName; // 获取包名
                Log.d("tag", "packageName = " + pName);

                //打开相应地图并且导航
                if (appName.equals("百度地图")) {
                    openBaiduMap(destnation);
                } else if (appName.equals("高德地图")) {
                    openGaodeMap(destnation);
                }

                Toast.makeText(this, "已为您打开" + appName, Toast.LENGTH_LONG).show();
                return;
            }
        }

        Toast.makeText(this, "您没有安装" + appName, Toast.LENGTH_SHORT).show();
    }

    /**
     * 利用正则表达式判断字符串是否是数字
     *
     * @param str
     * @return
     */
    private boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        return isNum.matches();
    }

    /**
     * @param phoneNumberOrName 电话号码或姓名
     * @param message           短信内容
     */
    public void doSendSMSTo(String phoneNumberOrName, String message) {
        //数字
        if (isNumeric(phoneNumberOrName)) {
            Log.i(TAG, "phoneNumberOrName " + phoneNumberOrName);
            Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + phoneNumberOrName));
            intent.putExtra("sms_body", message);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            //发送短信方法二, 直接发送出去
            //sendMessage(number, message);
        } else { //姓名
            List<ContactInfo> contactLists = getContactLists(this);
            if (contactLists.isEmpty()) {
                Toast.makeText(this, "通讯录为空", Toast.LENGTH_SHORT).show();
                return;
            }

            for (ContactInfo contactInfo : contactLists) {
                if (phoneNumberOrName.equals(contactInfo.getName())) {
                    //根据姓名取出电话号码
                    String number = contactInfo.getNumber();
                    Log.i(TAG, "根据姓名取出电话号码 " + number);

                    //发送短信方法一，需要手动触发发送
                    Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + number));
                    intent.putExtra("sms_body", message);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    //发送短信方法二, 直接发送出去
                    //sendMessage(number, message);
                    return;
                }
            }
        }
    }

    // 另一种发短信方法，发送短信的内容
    private void sendMessage(String msg_number, String content) {
        SmsManager manager = SmsManager.getDefault();
        ArrayList<String> list = manager.divideMessage(content);  // 因为一条短信有字数限制，因此要将长短信拆分
        for (String text : list) {
            manager.sendTextMessage(msg_number, null, text, null, null);
        }
    }

    /**
     * 打开百度地图导航
     *
     * @param destnation 目的地
     */
    private void openBaiduMap(String destnation) {
        Intent i1 = new Intent();
        i1.setData(Uri.parse("baidumap://map/navi?query=" + destnation));
        i1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i1);
    }

    /**
     * 打开高德地图导航
     *
     * @param destnation 目的地
     */
    private void openGaodeMap(String destnation) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        //String url = "androidamap://navi?sourceApplication=amap"+"&dname="+"新街口"+"&dev=0&t=1";
        String url = "amapuri://route/plan/?" + "dname=" + destnation + "&dev=0&t=0";
        Uri uri = Uri.parse(url);
        //将功能Scheme以URI的方式传入data
        intent.setData(uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //启动该页面即可
        startActivity(intent);
    }

    // 打电话
    private void call(String phoneOrName) {
        if (TextUtils.isEmpty(phoneOrName)) return;
        if (isNumeric(phoneOrName)) {
            Toast.makeText(this, "已经为您拨通 " + phoneOrName
                    , Toast.LENGTH_SHORT).show();
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_CALL);
            //intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setData(Uri.parse("tel:" + phoneOrName));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {
            List<ContactInfo> contactLists = getContactLists(this);
            if (contactLists.isEmpty()) {
                Toast.makeText(this, "通讯录为空", Toast.LENGTH_SHORT).show();
                return;
            }
            for (ContactInfo contactInfo : contactLists) {
                if (phoneOrName.equals(contactInfo.getName())) {
                    Toast.makeText(this, "已经为您拨通 " + contactInfo.getName() + " 的电话"
                            , Toast.LENGTH_SHORT).show();
                    String number = contactInfo.getNumber();
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_CALL);
                    //intent.addCategory(Intent.CATEGORY_DEFAULT);
                    intent.setData(Uri.parse("tel:" + number));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    return;
                }
            }

            Toast.makeText(this, "通讯录中没有此人", Toast.LENGTH_SHORT).show();
        }
    }

    // 获取通信录中所有的联系人
    private List<ContactInfo> getContactLists(Context context) {
        List<ContactInfo> lists = new ArrayList<ContactInfo>();
        Cursor cursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null, null, null, null);
        //moveToNext 方法返回的是一个 boolean 类型的数据
        while (cursor.moveToNext()) {
            // 读取通讯录的姓名
            String name = cursor.getString(cursor
                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            // 读取通讯录的号码
            String number = cursor.getString(cursor
                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            ContactInfo contactInfo = new ContactInfo(name, number);
            lists.add(contactInfo);
        }
        cursor.close();
        return lists;
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
        Logger.setHandler(recogHandler);

        /*if (myWakeup != null) return;//防止多实例
        IWakeupListener listener = new SimpleWakeupListener();
        myWakeup = new MyWakeup(this, listener);

        if (myRecognizer != null) return; //防止多实例
        StatusRecogListener recogListener = new MessageStatusRecogListener(recogHandler);
        myRecognizer = new MyRecognizer(this, recogListener);*/
        StatusRecogListener recogListener = new MessageStatusRecogListener(recogHandler);
        // 改为 SimpleWakeupListener 后，不依赖handler，但将不会在UI界面上显示
        myRecognizer = new MyRecognizer(this, recogListener);

        IWakeupListener listener = new RecogWakeupListener(recogHandler);
        // 改为 SimpleWakeupListener 后，不依赖handler，但将不会在UI界面上显示
        myWakeup = new MyWakeup(this, listener);

        //启动唤醒
        start();
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
        if (myWakeup != null) myWakeup.release();
        if (myRecognizer != null) myRecognizer.release();
        Log.i(TAG, "释放资源成功");
        super.onDestroy();
    }

}
