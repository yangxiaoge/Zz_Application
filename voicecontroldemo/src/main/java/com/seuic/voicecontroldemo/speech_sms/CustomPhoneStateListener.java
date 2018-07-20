package com.seuic.voicecontroldemo.speech_sms;

import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * Created by yangjianan on 2018/7/16.
 * 来去电监听
 */
public class CustomPhoneStateListener extends PhoneStateListener {
    private static final String TAG = CustomPhoneStateListener.class.getSimpleName();
    private Context mContext;
    private IncomingPhoneCallback mIncomingPhoneCallback;

    CustomPhoneStateListener(Context context, IncomingPhoneCallback incomingPhoneCallback) {
        mContext = context;
        mIncomingPhoneCallback = incomingPhoneCallback;
    }

    @Override
    public void onServiceStateChanged(ServiceState serviceState) {
        super.onServiceStateChanged(serviceState);
        Log.d(TAG, "CustomPhoneStateListener onServiceStateChanged: " + serviceState);
    }

    @Override
    public void onCallStateChanged(int state, String incomingNumber) {
        /*Toast.makeText(mContext, "CustomPhoneStateListener state: "
                + state + " incomingNumber: " + incomingNumber, Toast.LENGTH_SHORT).show();*/
        Log.d(TAG, "CustomPhoneStateListener state: "
                + state + " incomingNumber: " + incomingNumber);
        switch (state) {
            case TelephonyManager.CALL_STATE_IDLE:      // 电话挂断
                Log.d(TAG, "电话挂断啦");
                break;
            case TelephonyManager.CALL_STATE_RINGING:   // 电话响铃
                Log.d(TAG, "电话响铃啦");
                mIncomingPhoneCallback.phoneNumber(incomingNumber);
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:   // 来电接通 或者 去电，去电接通  但是没法区分
                Log.d(TAG, "电话接通啦");
                break;
        }
    }

    interface IncomingPhoneCallback {
        void phoneNumber(String text);
    }
}