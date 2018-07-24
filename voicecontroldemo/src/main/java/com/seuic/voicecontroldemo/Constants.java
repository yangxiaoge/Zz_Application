package com.seuic.voicecontroldemo;

import android.os.Environment;

import java.io.File;

/**
 * Created by yangjianan on 2018/7/17.
 */
public class Constants {
    //TTS Speak的相关文件目录名称
    public static final String TTSDIR_NAME = "seuicbaiduTTS";
    //ASR 语音识别相关文件目录名称
    public static final String RECONGNITIONDIR_NAME = "seuicbaiduASR";

    //日志路径
    public static final String APP_FILE_PATH = Environment.getExternalStorageDirectory()
            + File.separator + "seuicVoiceControlLog";
    public static final String LOG_PATH = APP_FILE_PATH + File.separator + "log";
}
