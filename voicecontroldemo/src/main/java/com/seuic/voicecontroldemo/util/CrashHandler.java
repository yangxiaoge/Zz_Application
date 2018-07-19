
package com.seuic.voicecontroldemo.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.seuic.voicecontroldemo.Constants;

import org.slf4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Crash info collection
 */
public class CrashHandler implements UncaughtExceptionHandler {

    private static final String LOG_TAG = CrashHandler.class.getSimpleName();

    private static CrashHandler me = null;

    private Context mContext;

    private Map<String, String> infos = new HashMap<String, String>();

    private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault());

    // 系统默认的 UncaughtException 处理类
    private Thread.UncaughtExceptionHandler mDefaultHandler;

    private CrashHandler() {
    }

    public static CrashHandler getInstance() {
        if (me == null) {
            me = new CrashHandler();
        }
        return me;
    }

    public void init(Context context) {
        mContext = context;
        // 获取系统默认的 UncaughtException 处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();

        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        Log.e(LOG_TAG, ex.getMessage(), ex);
        if (!handleException(ex) && mDefaultHandler != null) {
            // 如果用户没有处理则让系统默认的异常处理器来处理
            mDefaultHandler.uncaughtException(thread, ex);
        } else {

            Intent intent = mContext.getPackageManager().getLaunchIntentForPackage(mContext.getPackageName());
//        Intent intent = new Intent(mContext.getApplicationContext(), SplashActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent restartIntent = PendingIntent.getActivity(
                    mContext.getApplicationContext(), 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            //退出程序
            AlarmManager mgr = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
            mgr.set(AlarmManager.RTC, System.currentTimeMillis(),
                    restartIntent); // 1秒钟后重启应用

            try {
                Thread.sleep(4500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

//        Intent intent = new Intent(mContext, SplashActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        mContext.startActivity(intent);

            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
        }
    }

    private boolean handleException(Throwable ex) {

        if (ex == null) {
            return false;
        }

        // 使用 Toast 来显示异常信息
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                Toast.makeText(mContext, "很抱歉,程序发生错误即将重启.", Toast.LENGTH_LONG).show();
                Looper.loop();
            }
        }.start();

        // 收集设备参数信息
        collectDeviceInfo(mContext);
        // 保存日志文件
//        saveCrashInfoByLog(ex);
        saveCrashInfo2File(ex);
        return true;

//        if (ex != null) {
//
//            collectDeviceInfo(mContext);
////            saveCrashInfo2File(ex);
//            saveCrashInfoByLog(ex);
//            return true;
//        }
//        return false;
    }

    Logger logger = Logs.CRASH;

    private void collectDeviceInfo(Context ctx) {
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                String versionName = pi.versionName == null ? "null" : pi.versionName;
                String versionCode = pi.versionCode + "";
                infos.put("versionName", versionName);
                infos.put("versionCode", versionCode);
            }
        } catch (NameNotFoundException e) {
        }
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                infos.put(field.getName(), field.get(null).toString());
                //Log.d(LOG_TAG, field.getName() + " : " + field.get(null));
            } catch (Exception e) {
            }
        }
    }


    private String saveCrashInfoByLog(Throwable ex) {

        StringBuffer sb = new StringBuffer();
        for (Map.Entry<String, String> entry : infos.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key + "=" + value + "\n");
        }
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String result = writer.toString();
        sb.append(result);
        try {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                logger.trace(sb.toString());
            }
            return logger.getName();
        } catch (Exception e) {
            Log.e(LOG_TAG, "an error occured while writing file...", e);
        }
        return null;
    }

    private String saveCrashInfo2File(Throwable ex) {

        StringBuffer sb = new StringBuffer();
        for (Map.Entry<String, String> entry : infos.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key + "=" + value + "\n");
        }

        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String result = writer.toString();
        sb.append(result);
        try {
            long timestamp = System.currentTimeMillis();
            String time = formatter.format(new Date());
            String fileName = "crash-" + time + "-" + timestamp + ".txt";
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                String path = Constants.LOG_PATH + File.separator + "crash";
                File dir = new File(path);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                FileOutputStream fos = new FileOutputStream(path + fileName);
                fos.write(sb.toString().getBytes());
                fos.close();
            }
            return fileName;
        } catch (Exception e) {
            Log.e(LOG_TAG, "an error occured while writing file...", e);
        }
        return null;
    }
}
