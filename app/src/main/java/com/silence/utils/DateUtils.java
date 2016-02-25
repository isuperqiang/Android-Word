package com.silence.utils;

/**
 * Created by Silence on 2016/2/9 0009.
 */
public class DateUtils {

    private DateUtils() {
    }

    public static String formatTime(long duration) {
        long hour = duration / (60 * 60 * 1000);
        long min = (duration % (60 * 60 * 1000)) / (60 * 1000);
        long sec = (duration % (60 * 1000)) / 1000;
        return (hour != 0 ? hour + "小时" : "") + (min != 0 ? min + "分钟" : "") + sec + "秒";
    }
}
