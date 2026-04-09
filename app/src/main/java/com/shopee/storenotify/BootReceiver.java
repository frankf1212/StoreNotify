package com.shopee.storenotify;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.text.TextUtils;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = context.getSharedPreferences("StoreNotifyPrefs", Context.MODE_PRIVATE);
        String storeName = prefs.getString(MainActivity.KEY_STORE_NAME, "");
        if (!TextUtils.isEmpty(storeName)) {
            Intent serviceIntent = new Intent(context, NotificationService.class);
            serviceIntent.putExtra(MainActivity.KEY_STORE_NAME, storeName);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
            } else {
                context.startService(serviceIntent);
            }
        }
    }
}
