package com.suckhoe.app;

import android.app.Application;

import com.suckhoe.app.util.NotificationHelper;

public class HealthApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Khởi tạo Notification Channel cho Nhắc uống nước và Lịch tập
        NotificationHelper.createNotificationChannels(this);
        // Lên lịch nhắc uống nước ban đầu nếu được bật
        NotificationHelper.scheduleWaterReminder(this);
    }
}
