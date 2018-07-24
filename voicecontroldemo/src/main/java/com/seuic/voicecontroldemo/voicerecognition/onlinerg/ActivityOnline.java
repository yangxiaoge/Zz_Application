package com.seuic.voicecontroldemo.voicerecognition.onlinerg;


import com.seuic.voicecontroldemo.voicerecognition.onlinerg.online.OnlineRecogParams;

/**
 * 在线识别，用于展示在线情况下的识别参数和效果。
 * <p>
 * 本类可以忽略
 * ActivityRecog 识别流程，看下ActivityRecog开头的注释
 */
public class ActivityOnline extends ActivityRecog {
    {
        descText = "请授予所有的权限以便正常使用\n"
                + "请保持设备联网，对着麦克风说出关键词指令\n\n"
                + "指令示例：\n"
                + "\t打开百度（高德）地图导航去新街口\n"
                + "\t打电话给张三（13892153901） / 给张三打电话\n"
                + "\t挂断/拒接 电话\n"
                + "\t给XXX发送短信，短信内容是XXXX / 发送短信给XXX，短信内容是XXXX\n";
    }

    public ActivityOnline() {
        super();
        settingActivityClass = OnlineSetting.class;
    }

    @Override
    protected CommonRecogParams getApiParams() {
        return new OnlineRecogParams(this);
    }


}