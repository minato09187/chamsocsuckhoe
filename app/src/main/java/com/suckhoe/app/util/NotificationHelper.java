package com.suckhoe.app.util;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;

import androidx.core.app.NotificationCompat;

import com.suckhoe.app.MainActivity;
import com.suckhoe.app.R;
import com.suckhoe.app.receiver.WaterReminderReceiver;

public class NotificationHelper {
    public static final String WATER_CHANNEL_ID = "water_reminder_channel";
    public static final String WORKOUT_CHANNEL_ID = "workout_schedule_channel";

    public static void createNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager == null) return;

            // Kênh thông báo uống nước
            NotificationChannel waterChannel = new NotificationChannel(
                    WATER_CHANNEL_ID,
                    "Nhắc nhở uống nước",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            waterChannel.setDescription("Nhắc nhở bạn bổ sung nước đầy đủ trong ngày");
            waterChannel.enableVibration(true);
            manager.createNotificationChannel(waterChannel);

            // Kênh thông báo lịch tập
            NotificationChannel workoutChannel = new NotificationChannel(
                    WORKOUT_CHANNEL_ID,
                    "Lịch tập luyện",
                    NotificationManager.IMPORTANCE_HIGH
            );
            workoutChannel.setDescription("Thông báo lịch tập thể hình & thể thao");
            workoutChannel.enableVibration(true);
            manager.createNotificationChannel(workoutChannel);
        }
    }

    public static void showWaterNotification(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0)
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, WATER_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_water)
                .setContentTitle(context.getString(R.string.water_notification_title))
                .setContentText(context.getString(R.string.water_notification_msg))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(1001, builder.build());
        }
    }

    public static void scheduleWaterReminder(Context context) {
        PreferenceManager pref = new PreferenceManager(context);
        if (!pref.isWaterReminderEnabled()) {
            cancelWaterReminder(context);
            return;
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, WaterReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 100, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0)
        );

        int intervalMinutes = pref.getWaterReminderIntervalMinutes();
        long intervalMillis = (long) intervalMinutes * 60 * 1000L;
        long triggerTime = SystemClock.elapsedRealtime() + intervalMillis;

        if (alarmManager != null) {
            alarmManager.setInexactRepeating(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    triggerTime,
                    intervalMillis,
                    pendingIntent
            );
        }
    }

    public static void cancelWaterReminder(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, WaterReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 100, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0)
        );
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }
}
