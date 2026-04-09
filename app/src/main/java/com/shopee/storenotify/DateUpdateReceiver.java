package com.shopee.storenotify;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DateUpdateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // When system date changes, ask the running service to refresh
        Intent serviceIntent = new Intent(context, NotificationService.class);
        context.startService(serviceIntent);
    }
}
