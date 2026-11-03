package com.suckhoe.app.util;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {
    private static final String PREF_NAME = "CSSK_Prefs";

    // User health profile
    public static final String KEY_USER_HEIGHT = "user_height";
    public static final String KEY_DAILY_CALORIE_GOAL = "daily_calorie_goal";
    public static final String KEY_DAILY_WATER_GOAL = "daily_water_goal";
    public static final String KEY_WATER_REMINDER_ENABLED = "water_reminder_enabled";
    public static final String KEY_WATER_REMINDER_INTERVAL_MINS = "water_reminder_interval_mins";

    private final SharedPreferences prefs;

    public PreferenceManager(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public float getUserHeight() {
        return prefs.getFloat(KEY_USER_HEIGHT, 170.0f);
    }

    public void setUserHeight(float height) {
        prefs.edit().putFloat(KEY_USER_HEIGHT, height).apply();
    }

    public int getDailyCalorieGoal() {
        return prefs.getInt(KEY_DAILY_CALORIE_GOAL, 2000);
    }

    public void setDailyCalorieGoal(int goal) {
        prefs.edit().putInt(KEY_DAILY_CALORIE_GOAL, goal).apply();
    }

    public int getDailyWaterGoal() {
        return prefs.getInt(KEY_DAILY_WATER_GOAL, 2000);
    }

    public void setDailyWaterGoal(int goal) {
        prefs.edit().putInt(KEY_DAILY_WATER_GOAL, goal).apply();
    }

    public boolean isWaterReminderEnabled() {
        return prefs.getBoolean(KEY_WATER_REMINDER_ENABLED, true);
    }

    public void setWaterReminderEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_WATER_REMINDER_ENABLED, enabled).apply();
    }

    public int getWaterReminderIntervalMinutes() {
        return prefs.getInt(KEY_WATER_REMINDER_INTERVAL_MINS, 60);
    }

    public void setWaterReminderIntervalMinutes(int minutes) {
        prefs.edit().putInt(KEY_WATER_REMINDER_INTERVAL_MINS, minutes).apply();
    }
}
