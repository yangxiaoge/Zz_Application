package com.bruce.kotlin.readhub.api

import android.text.TextUtils
import android.widget.Toast

import com.bruce.kotlin.readhub.Application
import com.yjn.common.util.ToastUtil
import com.yjn.common.util.Util

/**
 * Created by yangjianan on 2017/12/12.
 */

object HeaderConfig {
    // 渠道类型 1-Android 2-IOS,必填
    val channel = "1"
    // 设备号,必填
    var deviceNo: String = ""

    var token = ""

    fun getDeviceNo1(): String {
        if (TextUtils.isEmpty(deviceNo)) {
            val deviceNoNow: String
            deviceNoNow = Util.getMacAddress()
            if (TextUtils.isEmpty(deviceNoNow)) {
                ToastUtil.show(Application.getAppContext(), "网络出错，请检查网络", Toast.LENGTH_SHORT)
                return ""
            } else {
                deviceNo = deviceNoNow
            }
        }
        return deviceNo
    }
}
