package com.seuic.voicecontroldemo.speech_sms;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

/**
 * 监测短信
 */
public class SmsObserver extends ContentObserver {
    public static final Uri SMS_INBOX = Uri.parse("content://sms/inbox");
    private Context mContext;
    private SmsSpeakCallback smsSpeakCallback;

    SmsObserver(Context context, Handler handler, SmsSpeakCallback smsSpeakCallback) {
        super(handler);
        this.mContext = context;
        this.smsSpeakCallback = smsSpeakCallback;
    }

    interface SmsSpeakCallback {
        void smsText(String text);
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        String code = "";
        String[] projection = new String[]{"body", "address"};//"_id", "address", "person",, "date", "type
        Cursor cur = mContext.getContentResolver().query(SMS_INBOX, projection, null, null, "date desc");

        if (cur != null) {
            if (cur.moveToFirst()) {
                String address = cur.getString(cur.getColumnIndex("address"));
                String body = cur.getString(cur.getColumnIndex("body"));

                smsSpeakCallback.smsText(body);
                Log.i("TAG", "发件人为: " + address + "\n" + "短信内容: " + body);
            }
            cur.close();
        }
    }

}