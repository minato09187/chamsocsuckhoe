package com.suckhoe.app.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtils {
    private static final SimpleDateFormat ISO_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private static final SimpleDateFormat DISPLAY_DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public static String getTodayIsoDate() {
        return ISO_DATE_FORMAT.format(new Date());
    }

    public static String formatDisplayDate(String isoDate) {
        if (isoDate == null || isoDate.isEmpty()) return "";
        try {
            Date date = ISO_DATE_FORMAT.parse(isoDate);
            if (date != null) {
                return DISPLAY_DATE_FORMAT.format(date);
            }
        } catch (Exception ignored) {}
        return isoDate;
    }

    public static String getDayOfWeekName() {
        Calendar cal = Calendar.getInstance();
        int day = cal.get(Calendar.DAY_OF_WEEK);
        switch (day) {
            case Calendar.MONDAY: return "Thứ 2";
            case Calendar.TUESDAY: return "Thứ 3";
            case Calendar.WEDNESDAY: return "Thứ 4";
            case Calendar.THURSDAY: return "Thứ 5";
            case Calendar.FRIDAY: return "Thứ 6";
            case Calendar.SATURDAY: return "Thứ 7";
            case Calendar.SUNDAY: return "Chủ Nhật";
            default: return "Hôm nay";
        }
    }

    public static String getCurrentTime() {
        return TIME_FORMAT.format(new Date());
    }
}

