package com.seuic.voicecontroldemo.util;

import android.content.Context;
import android.os.IBinder;
import android.widget.Toast;

import com.android.internal.telephony.ITelephony;

import java.lang.reflect.Method;

/**
 * Created by yangjianan on 2018/7/19.
 * 挂断电话工具类,通过aidl实现
 * https://blog.csdn.net/l1028386804/article/details/47072451
 */
public class PhoneUtils {
    //挂断电话
    public static void endCall(Context context) {
        try {
            Class<?> clazz = Class.forName("android.os.ServiceManager");
            Method method = clazz.getMethod("getService", String.class);
            IBinder ibinder = (IBinder) method.invoke(null, Context.TELEPHONY_SERVICE);
            ITelephony iTelephony = ITelephony.Stub.asInterface(ibinder);
            iTelephony.endCall();

            Toast.makeText(context, "已成功为您挂断", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
