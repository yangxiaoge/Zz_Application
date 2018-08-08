package com.yjn.common.util;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

/**
 * Toast统一管理类
 */
public class ToastUtil {


    private static Toast toast;
    private static Toast toast2;

    private static Toast initToast(Context mContext, CharSequence message, int duration) {
        if (toast == null) {
            toast = Toast.makeText(mContext, message, duration);
            toast.setGravity(Gravity.BOTTOM | Gravity.CENTER, 0, (int) Util.dip2px(mContext,50));
        } else {
            toast.setText(message);
            toast.setDuration(duration);
        }
        return toast;
    }

    /**
     * 短时间显示Toast
     *
     * @param message
     */
    public static void showShort(Context mContext, CharSequence message) {
        initToast(mContext, message, Toast.LENGTH_SHORT).show();
    }


    /**
     * 短时间显示Toast
     *
     * @param strResId
     */
    public static void showShort(Context mContext, int strResId) {
//		Toast.makeText(context, strResId, Toast.LENGTH_SHORT).show();
        initToast(mContext, mContext.getResources().getText(strResId), Toast.LENGTH_SHORT).show();
    }

    /**
     * 长时间显示Toast
     *
     * @param message
     */
    public static void showLong(Context mContext, CharSequence message) {
        initToast(mContext, message, Toast.LENGTH_LONG).show();
    }

    /**
     * 长时间显示Toast
     *
     * @param strResId
     */
    public static void showLong(Context mContext, int strResId) {
        initToast(mContext, mContext.getResources().getText(strResId), Toast.LENGTH_LONG).show();
    }

    /**
     * 自定义显示Toast时间
     *
     * @param message
     * @param duration
     */
    public static void show(Context mContext, CharSequence message, int duration) {
        initToast(mContext, message, duration).show();
    }

    /**
     * 自定义显示Toast时间
     *
     * @param context
     * @param strResId
     * @param duration
     */
    public static void show(Context context, int strResId, int duration) {
        initToast(context, context.getResources().getText(strResId), duration).show();
    }

}
