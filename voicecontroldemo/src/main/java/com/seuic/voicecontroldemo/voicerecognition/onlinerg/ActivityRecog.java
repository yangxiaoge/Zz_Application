package com.seuic.voicecontroldemo.voicerecognition.onlinerg;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.baidu.speech.asr.SpeechConstant;
import com.seuic.voicecontroldemo.util.PhoneUtils;
import com.seuic.voicecontroldemo.voicerecognition.ContactInfo;
import com.seuic.voicecontroldemo.voicerecognition.control.MyRecognizer;
import com.seuic.voicecontroldemo.voicerecognition.onlinerg.offline.OfflineRecogParams;
import com.seuic.voicecontroldemo.voicerecognition.rg.IStatus;
import com.seuic.voicecontroldemo.voicerecognition.rg.MessageStatusRecogListener;
import com.seuic.voicecontroldemo.voicerecognition.rg.StatusRecogListener;
import com.seuic.voicecontroldemo.voicerecognition.util.AutoCheck;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 识别的基类Activity。 ActivityCommon定义了通用的UI部分
 * 封装了识别的大部分逻辑，包括MyRecognizer的初始化，资源释放
 * <p>
 * <p>
 * 集成流程代码，只需要一句： myRecognizer.start(params);具体示例代码参见startRough()
 * =》.实例化 myRecognizer   new MyRecognizer(this, listener);
 * =》 实例化 listener  new MessageStatusRecogListener(null);
 * </p>
 * 集成文档： http://ai.baidu.com/docs#/ASR-Android-SDK/top 集成指南一节
 * demo目录下doc_integration_DOCUMENT
 * ASR-INTEGRATION-helloworld  ASR集成指南-集成到helloworld中 对应 ActivityMiniRecog
 * ASR-INTEGRATION-TTS-DEMO ASR集成指南-集成到合成DEMO中 对应 ActivityRecog
 * <p>
 * 大致流程为
 * 1. 实例化MyRecognizer ,调用release方法前不可以实例化第二个。参数中需要开发者自行填写语音识别事件的回调类，实现开发者自身的业务逻辑
 * 2. 如果使用离线命令词功能，需要调用loadOfflineEngine。在线功能不需要。
 * 3. 根据识别的参数文档，或者demo中测试出的参数，组成json格式的字符串。调用 start 方法
 * 4. 在合适的时候，调用release释放资源。
 * <p>
 */

public abstract class ActivityRecog extends ActivityCommon implements IStatus {

    /**
     * 识别控制器，使用MyRecognizer控制识别的流程
     */
    protected MyRecognizer myRecognizer;

    /*
     * Api的参数类，仅仅用于生成调用START的json字符串，本身与SDK的调用无关
     */
    protected CommonRecogParams apiParams;

    /*
     * 本Activity中是否需要调用离线命令词功能。根据此参数，判断是否需要调用SDK的ASR_KWS_LOAD_ENGINE事件
     */
    protected boolean enableOffline = false;


    /**
     * 控制UI按钮的状态
     */
    protected int status;

    /**
     * 日志使用
     */
    private static final String TAG = "ActivityRecog";

    /**
     * 在onCreate中调用。初始化识别控制类MyRecognizer
     */
    protected void initRecog() {
        StatusRecogListener listener = new MessageStatusRecogListener(handler);
        myRecognizer = new MyRecognizer(this, listener);
        apiParams = getApiParams();
        status = STATUS_NONE;
        if (enableOffline) {
            myRecognizer.loadOfflineEngine(OfflineRecogParams.fetchOfflineParams());
        }
    }

    /**
     * 开始录音，点击“开始”按钮后调用。
     */
    protected void start() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ActivityRecog.this);
        //  上面的获取是为了生成下面的Map， 自己集成时可以忽略
        // / 集成时不需要上面的代码，只需要params参数。
        final Map<String, Object> params = apiParams.fetch(sp);
        // 复制此段可以自动检测错误
        (new AutoCheck(getApplicationContext(), new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what == 100) {
                    AutoCheck autoCheck = (AutoCheck) msg.obj;
                    synchronized (autoCheck) {
                        String message = autoCheck.obtainErrorMessage(); // autoCheck.obtainAllMessage();
                        txtLog.append(message + "\n");
                        ; // 可以用下面一行替代，在logcat中查看代码
                        // Log.w("AutoCheckMessage", message);
                    }
                }
            }
        }, enableOffline)).checkAsr(params);

        // 这里打印出params， 填写至您自己的app中，直接调用下面这行代码即可。
        myRecognizer.start(params);
    }

    /**
     * 测试demo成功后可以修改这个方法
     * 粗略测试，将原来的start方法注释，这个方法改为start即可。
     * 点击开始按钮使用，注意此时与本demo的UI已经解绑，UI上不会显示，请自行看logcat日志
     */
    protected void startRough() {
        // initRecog中已经初始化，这里释放。不需要集成到您的代码中
        myRecognizer.release();
        myRecognizer = null;
        // 上面不需要集成到您的代码中

        /*********************************************/
        // 1. 确定识别参数
        Map<String, Object> params = new LinkedHashMap<String, Object>();
        params.put(SpeechConstant.ACCEPT_AUDIO_VOLUME, false);
        // 具体的params的值在 测试demo成功后，myRecognizer.start(params);中打印

        // 2. 初始化IRecogListener
        StatusRecogListener listener = new MessageStatusRecogListener(null);
        // 日志显示在logcat里，UI界面上是没有的。需要显示在界面上， 这里设置为handler

        // 3 初始化 MyRecognizer
        myRecognizer = new MyRecognizer(this, listener);

        // 4. 启动识别
        myRecognizer.start(params);
        // 日志显示在logcat里，UI界面上是没有的。

        // 5 识别结束了别忘了释放。

        // 需要离线识别过程，需要加上 myRecognizer.loadOfflineEngine(OfflineRecogParams.fetchOfflineParams());
        // 注意这个loadOfflineEngine是异步的， 不能连着调用 start
    }

    /**
     * 销毁时需要释放识别资源。
     */
    @Override
    protected void onDestroy() {
        myRecognizer.release();
        Log.i(TAG, "onDestory");
        super.onDestroy();
    }

    /**
     * 开始录音后，手动停止录音。SDK会识别在此过程中的录音。点击“停止”按钮后调用。
     */
    private void stop() {
        myRecognizer.stop();
    }

    /**
     * 开始录音后，取消这次录音。SDK会取消本次识别，回到原始状态。点击“取消”按钮后调用。
     */
    private void cancel() {
        myRecognizer.cancel();
    }


    /**
     * @return
     */
    protected abstract CommonRecogParams getApiParams();

    // 以上为 语音SDK调用，以下为UI部分
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    protected void initView() {
        super.initView();
        btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                switch (status) {
                    case STATUS_NONE: // 初始状态
                        start();
                        status = STATUS_WAITING_READY;
                        updateBtnTextByStatus();
                        txtLog.setText("");
                        txtResult.setText("");
                        break;
                    case STATUS_WAITING_READY: // 调用本类的start方法后，即输入START事件后，等待引擎准备完毕。
                    case STATUS_READY: // 引擎准备完毕。
                    case STATUS_SPEAKING:
                    case STATUS_FINISHED: // 长语音情况
                    case STATUS_RECOGNITION:
                        stop();
                        status = STATUS_STOPPED; // 引擎识别中
                        updateBtnTextByStatus();
                        break;
                    case STATUS_STOPPED: // 引擎识别中
                        cancel();
                        status = STATUS_NONE; // 识别结束，回到初始状态
                        updateBtnTextByStatus();
                        break;
                    default:
                        break;
                }

            }
        });
    }

    protected void handleMsg(Message msg) {
        super.handleMsg(msg);

        switch (msg.what) { // 处理MessageStatusRecogListener中的状态回调
            case STATUS_FINISHED:
                if (msg.arg2 == 1) {
                    txtResult.setText(msg.obj.toString());

                    String str = msg.obj.toString().trim();
                    dealWithMessage(str);
                }
                status = msg.what;
                updateBtnTextByStatus();
                break;
            case STATUS_NONE:
            case STATUS_READY:
            case STATUS_SPEAKING:
            case STATUS_RECOGNITION:
                status = msg.what;
                updateBtnTextByStatus();
                break;
            default:
                break;

        }
    }

    /**
     * 处理语音结果
     *
     * @param str message
     */
    private void dealWithMessage(String str) {
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

    private void updateBtnTextByStatus() {
        switch (status) {
            case STATUS_NONE:
                btn.setText("开始录音");
                btn.setEnabled(true);
                setting.setEnabled(true);
                break;
            case STATUS_WAITING_READY:
            case STATUS_READY:
            case STATUS_SPEAKING:
            case STATUS_RECOGNITION:
                btn.setText("停止录音");
                btn.setEnabled(true);
                setting.setEnabled(false);
                break;

            case STATUS_STOPPED:
                btn.setText("取消整个识别过程");
                btn.setEnabled(true);
                setting.setEnabled(false);
                break;
            default:
                break;
        }
    }
}
