package com.suckhoe.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.suckhoe.app.util.NotificationHelper;

public class WaterReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationHelper.showWaterNotification(context);
    }
}
