package com.suckhoe.app.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.suckhoe.app.data.model.User;

public class PreferenceManager {
    private static final String PREF_NAME = "CSSK_Prefs";

    // User session
    public static final String KEY_CURRENT_USER_ID = "current_user_id";
    public static final String KEY_CURRENT_USERNAME = "current_username";
    public static final String KEY_CURRENT_FULLNAME = "current_fullname";
    public static final String KEY_CURRENT_EMAIL = "current_email";

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

    public void saveUserSession(User user) {
        if (user != null && user.getId() != null) {
            prefs.edit()
                    .putLong(KEY_CURRENT_USER_ID, user.getId())
                    .putString(KEY_CURRENT_USERNAME, user.getUsername())
                    .putString(KEY_CURRENT_FULLNAME, user.getFullName())
                    .putString(KEY_CURRENT_EMAIL, user.getEmail())
                    .apply();
        }
    }

    public boolean isLoggedIn() {
        return getCurrentUserId() > 0;
    }

    public long getCurrentUserId() {
        return prefs.getLong(KEY_CURRENT_USER_ID, -1L);
    }

    public String getCurrentUsername() {
        return prefs.getString(KEY_CURRENT_USERNAME, "");
    }

    public String getCurrentUserFullName() {
        return prefs.getString(KEY_CURRENT_FULLNAME, "Người dùng");
    }

    public String getCurrentUserEmail() {
        return prefs.getString(KEY_CURRENT_EMAIL, "");
    }

    public void logout() {
        prefs.edit()
                .remove(KEY_CURRENT_USER_ID)
                .remove(KEY_CURRENT_USERNAME)
                .remove(KEY_CURRENT_FULLNAME)
                .remove(KEY_CURRENT_EMAIL)
                .apply();
    }

    public float getUserHeight() {
        return prefs.getFloat(KEY_USER_HEIGHT + "_" + getCurrentUserId(), 170.0f);
    }

    public void setUserHeight(float height) {
        prefs.edit().putFloat(KEY_USER_HEIGHT + "_" + getCurrentUserId(), height).apply();
    }

    public int getDailyCalorieGoal() {
        return prefs.getInt(KEY_DAILY_CALORIE_GOAL + "_" + getCurrentUserId(), 2000);
    }

    public void setDailyCalorieGoal(int goal) {
        prefs.edit().putInt(KEY_DAILY_CALORIE_GOAL + "_" + getCurrentUserId(), goal).apply();
    }

    public int getDailyWaterGoal() {
        return prefs.getInt(KEY_DAILY_WATER_GOAL + "_" + getCurrentUserId(), 2000);
    }

    public void setDailyWaterGoal(int goal) {
        prefs.edit().putInt(KEY_DAILY_WATER_GOAL + "_" + getCurrentUserId(), goal).apply();
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
