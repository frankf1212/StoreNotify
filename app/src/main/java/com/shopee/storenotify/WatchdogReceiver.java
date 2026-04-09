package com.shopee.storenotify;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.text.TextUtils;

public class WatchdogReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!isServiceRunning(context, NotificationService.class)) {
            // Service is dead — restart it
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
        } else {
            // Service is alive — schedule next watchdog check
            // (Service will reschedule itself via scheduleWatchdog,
            //  but we also reschedule here as a fallback)
            Intent nextWatchdog = new Intent(context, NotificationService.class);
            context.startService(nextWatchdog);
        }
    }

    @SuppressWarnings("deprecation")
    private boolean isServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (am == null) return false;
        for (ActivityManager.RunningServiceInfo info : am.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(info.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
