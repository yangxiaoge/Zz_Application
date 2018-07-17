package com.seuic.voicecontroldemo.voicerecognition;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.baidu.speech.asr.SpeechConstant;
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

                    // 关键词 "打开"
                    if (res.contains("打开")) {
                        String appName = res.substring(res.indexOf("开") + 1);
                        Log.d("tag app name", appName);
                        openApp(appName);
//                        openApp1(appName);
                        return;
                    }

                    // 关键词 "打电话"
                    if (res.contains("打电话")) {
                        String phoneOrName = res.substring(res.indexOf("打电话给") + 1);
                        call(phoneOrName);
                        return;
                    }
                }
            }
        }

    }

    // 打开应用
    private void openApp(String appName) {
        PackageManager packageManager = getPackageManager();
        // 获取手机里的应用列表
        List<PackageInfo> pInfo = packageManager.getInstalledPackages(0);
        for (int i = 0; i < pInfo.size(); i++) {
            PackageInfo p = pInfo.get(i);
            // 获取相关包的 <application> 中的 label 信息，也就是 --> 应用程序的名字
            String label = packageManager.getApplicationLabel(p.applicationInfo).toString();
            //Log.d("tag","label = " + label);
            if (label.contains(appName)) { // 比较 label
                //refresh(appName + " 已经为您打开 ", RECEIVER);
                String pName = p.packageName; // 获取包名
                Log.d("tag", "packageName = " + pName);

                Intent intent = packageManager.getLaunchIntentForPackage(pName);
                if (intent == null) {
                    Toast.makeText(this, "您没有安装" + appName, Toast.LENGTH_LONG).show();
                } else {
                    startActivity(intent);
                }
                return;
            }
        }

        Toast.makeText(this, "您没有安装" + appName, Toast.LENGTH_SHORT).show();
    }

    private void openApp1(String packageName) {
        try {
            PackageInfo pi = getPackageManager().getPackageInfo(packageName, 0);

            Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
            resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            resolveIntent.setPackage(pi.packageName);

            List<ResolveInfo> apps = getPackageManager().queryIntentActivities(resolveIntent, 0);

            ResolveInfo ri = apps.iterator().next();
            if (ri != null) {
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

    // 打电话
    private void call(String res) {
        List<ContactInfo> contactLists = getContactLists(this);
        if (contactLists.isEmpty()) {
            Toast.makeText(this, "通讯录为空", Toast.LENGTH_SHORT).show();
            return;
        }

        for (ContactInfo contactInfo : contactLists) {
            if (res.contains(contactInfo.getName())) {
                Toast.makeText(this, " 已经为您拨通 " + contactInfo.getName() + " 的电话 "
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

        Toast.makeText(this, " 通讯录中没有此人 ", Toast.LENGTH_SHORT).show();
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
        myWakeup.release();
        myRecognizer.release();
        Log.i(TAG, "释放资源成功");
        super.onDestroy();
    }

}
