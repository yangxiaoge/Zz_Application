package com.seuic.voicecontroldemo.speech_sms;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;

/**
 * 监测短信
 * 短信读取有点问题，可能会错乱
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
        String[] projection = new String[]{"body", "address", "person", "type", "date"};//"_id", "address", "person",, "date", "type"
        Cursor cur = mContext.getContentResolver().query(SMS_INBOX, projection,
                null, null, "date desc");

        if (cur != null) {
            if (cur.moveToFirst()) {
                try {
                    String address = cur.getString(cur.getColumnIndex("address"));
                    String body = cur.getString(cur.getColumnIndex("body"));
                    String type = cur.getString(cur.getColumnIndex("type"));
                    if (type.equals("1")) { // 1是接收到的，2是已发出 ，但是 3 是什么鬼？
                        Log.i("发件人： ", "发件人为: " + getSMSSenderName(address));
                    }
                    smsSpeakCallback.smsText(body);
//                    Toast.makeText(mContext, "type = " + type + " 发件人为: " + getSMSSenderName(address) + "\n" + "短信内容: " + body, Toast.LENGTH_SHORT).show();
                    Log.i("SmsObserver", "type = " + type + " 发件人为: " + getSMSSenderName(address) + "\n" + "短信内容: " + body);
                } catch (Exception e) {
                    Log.i("SmsObserver", "获取短信异常");
                    e.printStackTrace();
                }
            }
            cur.close();
        }
    }

    // 获取发送短信的人名
    private String getSMSSenderName(String address) {
        Cursor cursor = mContext.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null, null, null, null);
        //moveToNext 方法返回的是一个 boolean 类型的数据
        while (cursor.moveToNext()) {
            // 读取通讯录的姓名
            String name = cursor.getString(cursor
                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            // 读取通讯录的号码
            String number = cursor.getString(cursor
                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            if (number.equals(address)) {
                return name;
            }
        }

        cursor.close();
        return address;
    }

}