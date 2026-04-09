package com.shopee.storenotify;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import androidx.core.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class NotificationService extends Service {

    static final String CHANNEL_ID       = "store_notify_channel";
    static final int    NOTIF_ID         = 1001;
    static final String ACTION_WATCHDOG  = "com.shopee.storenotify.WATCHDOG";
    private static final String PREFS_NAME        = "StoreNotifyPrefs";
    private static final int    WATCHDOG_INTERVAL = 15 * 60 * 1000; // 15 minutes

    private String storeName = "";
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable midnightRunnable;

    // ── Lifecycle ──────────────────────────────────────────────────────────

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.hasExtra(MainActivity.KEY_STORE_NAME)) {
            storeName = intent.getStringExtra(MainActivity.KEY_STORE_NAME);
        } else {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            storeName = prefs.getString(MainActivity.KEY_STORE_NAME, "門市");
        }

        createNotificationChannel();
        startForeground(NOTIF_ID, buildNotification());
        scheduleMidnightRefresh();
        scheduleWatchdog();

        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        rescheduleRestart();
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        cancelWatchdog();
        rescheduleRestart();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }

    // ── Notification ───────────────────────────────────────────────────────

    void refreshNotification() {
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (nm != null) nm.notify(NOTIF_ID, buildNotification());
    }

    private Notification buildNotification() {
        String dateStr = new SimpleDateFormat("yyyy/MM/dd (E)", Locale.TRADITIONAL_CHINESE)
                .format(new Date());

        PendingIntent pi = PendingIntent.getActivity(
                this, 0,
                new Intent(this, MainActivity.class),
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_store)
                .setContentTitle(storeName)
                .setContentText(dateStr)
                .setOngoing(true)
                .setShowWhen(false)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(pi)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_ID,
                    "門市狀態通知",
                    NotificationManager.IMPORTANCE_HIGH);
            ch.setDescription("顯示門市名稱與當日日期");
            ch.setShowBadge(false);
            ch.enableLights(false);
            ch.enableVibration(false);
            ch.setSound(null, null);
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(ch);
        }
    }

    // ── Midnight date refresh ──────────────────────────────────────────────

    private void scheduleMidnightRefresh() {
        handler.removeCallbacksAndMessages(null);
        Calendar midnight = Calendar.getInstance();
        midnight.add(Calendar.DAY_OF_MONTH, 1);
        midnight.set(Calendar.HOUR_OF_DAY, 0);
        midnight.set(Calendar.MINUTE, 0);
        midnight.set(Calendar.SECOND, 5);
        midnight.set(Calendar.MILLISECOND, 0);
        long delay = midnight.getTimeInMillis() - System.currentTimeMillis();
        midnightRunnable = () -> {
            refreshNotification();
            scheduleMidnightRefresh();
        };
        handler.postDelayed(midnightRunnable, delay);
    }

    // ── Watchdog every 15 min ─────────────────────────────────────────────

    private void scheduleWatchdog() {
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;
        PendingIntent pi = watchdogIntent();
        long triggerAt = System.currentTimeMillis() + WATCHDOG_INTERVAL;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi);
        } else {
            am.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pi);
        }
    }

    private void cancelWatchdog() {
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (am != null) am.cancel(watchdogIntent());
    }

    private PendingIntent watchdogIntent() {
        Intent i = new Intent(this, WatchdogReceiver.class);
        i.setAction(ACTION_WATCHDOG);
        return PendingIntent.getBroadcast(
                this, 0, i,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
    }

    // ── Restart safety net ────────────────────────────────────────────────

    private void rescheduleRestart() {
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;
        Intent i = new Intent(this, BootReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(
                this, 99, i,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 3000, pi);
    }
}
