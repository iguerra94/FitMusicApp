package com.tecnologiasmoviles.iua.fitmusic.utils;

import java.util.Calendar;
import java.util.Date;

public class DateUtils {

    public static int getHourOfDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.HOUR_OF_DAY);
    }

    public static int getMinutesOfDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.MINUTE);
    }

    public static int getSecondsOfDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.SECOND);
    }

    public static Date setTimeOfDate(int hour, int min, int sec) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(0, 0, 0, hour, min, sec);
        return calendar.getTime();
    }

}