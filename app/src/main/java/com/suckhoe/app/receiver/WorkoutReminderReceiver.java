package com.suckhoe.app.receiver;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.suckhoe.app.MainActivity;
import com.suckhoe.app.R;
import com.suckhoe.app.util.NotificationHelper;

public class WorkoutReminderReceiver extends BroadcastReceiver {
    public static final String EXTRA_WORKOUT_TITLE = "workout_title";
    public static final String EXTRA_WORKOUT_TIME = "workout_time";

    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra(EXTRA_WORKOUT_TITLE);
        String time = intent.getStringExtra(EXTRA_WORKOUT_TIME);
        if (title == null) title = "Lịch tập hôm nay";

        Intent mainIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 101, mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0)
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NotificationHelper.WORKOUT_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_workout)
                .setContentTitle("Lịch tập: " + title)
                .setContentText("Chuẩn bị tinh thần tập luyện lúc " + (time != null ? time : "hôm nay") + " nhé!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify((int) System.currentTimeMillis(), builder.build());
        }
    }
}
