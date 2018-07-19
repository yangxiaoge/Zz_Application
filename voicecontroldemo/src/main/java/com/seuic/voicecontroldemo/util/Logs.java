package com.seuic.voicecontroldemo.util;


import com.seuic.voicecontroldemo.Constants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Logs {

    public static boolean logInit = init();

    static boolean init() {
        if (logInit) return true;
        System.setProperty("log.home", Constants.LOG_PATH);
        System.setProperty("log.root.level", Constants.LOG_PATH);
        return true;
    }

    public static Logger HTTP = LoggerFactory.getLogger("http"),

    BUSINESS = LoggerFactory.getLogger("business"),

    CRASH = LoggerFactory.getLogger("crash");
}
