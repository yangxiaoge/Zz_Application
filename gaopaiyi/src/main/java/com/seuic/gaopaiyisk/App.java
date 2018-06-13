package com.seuic.gaopaiyisk;

import android.app.Application;
import android.os.Environment;

import com.xapp.jjh.logtools.config.XLogConfig;
import com.xapp.jjh.logtools.logger.LogLevel;
import com.xapp.jjh.logtools.tools.XLog;

import java.io.File;

/**
 * Created by yangjianan on 2018/6/13.
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        XLog.init(getApplicationContext(),
                new XLogConfig()
                        //loglevel FULL 为显示 log ，NONE 为不显示 log
                        .setLogLevel(LogLevel.FULL)
                        // 文件日志以及崩溃日志文件的目录
                        .setLogDir(new File(Environment.getExternalStorageDirectory(), "GaopaiyiLog"))
                        // 崩溃日志文件标记名称
                        .setCrashLogTag("CrashLogTag")
                        // 是否云保存文件日志（非 crash 日志）
                        .setFileLogAllow(true)
                        // 普通文件日志标记名称
                        .setNormalLogTag("NormalLogTag")
                        // 日志文件扩展名，默认. txt
                        .setFileExtensionName(XLogConfig.DEFAULT_FILE_EXTENSION_NAME)
                        // 日志文件定期清理周期（单位毫秒），默认为一周（7*24*60*60*1000）
                        .setFileClearCycle(XLogConfig.DEFAULT_FILE_CLEAR_CYCLE)
                        // 是否保存崩溃日志
                        .setSaveCrashLog(true)
                        // 是否为普通日志信息添加消息框
                        .setMessageTable(true));

    }
}
