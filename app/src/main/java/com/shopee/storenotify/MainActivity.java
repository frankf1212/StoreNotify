package com.shopee.storenotify;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "StoreNotifyPrefs";
    static final String KEY_STORE_NAME = "store_name";

    private EditText etStoreName;
    private TextView tvCurrentStore;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etStoreName    = findViewById(R.id.etStoreName);
        tvCurrentStore = findViewById(R.id.tvCurrentStore);
        Button btnSave = findViewById(R.id.btnSave);
        Button btnStop = findViewById(R.id.btnStop);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String saved = prefs.getString(KEY_STORE_NAME, "");
        updateCurrentStoreLabel(saved);
        if (!TextUtils.isEmpty(saved)) {
            etStoreName.setText(saved);
        }

        btnSave.setOnClickListener(v -> saveAndStart());
        btnStop.setOnClickListener(v -> {
            stopService(new Intent(this, NotificationService.class));
            Toast.makeText(this, "通知已停止", Toast.LENGTH_SHORT).show();
        });

        // Auto-start if already configured
        if (!TextUtils.isEmpty(saved)) {
            startNotificationService(saved);
        }

        // Request battery optimization exemption (most important for reliability)
        checkBatteryOptimization();

        // Android 13+: request notification permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
        }
    }

    private void saveAndStart() {
        String name = etStoreName.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "請輸入門市名稱", Toast.LENGTH_SHORT).show();
            return;
        }
        prefs.edit().putString(KEY_STORE_NAME, name).apply();
        updateCurrentStoreLabel(name);
        startNotificationService(name);
        Toast.makeText(this, "已儲存並啟動通知", Toast.LENGTH_SHORT).show();
    }

    private void startNotificationService(String name) {
        Intent intent = new Intent(this, NotificationService.class);
        intent.putExtra(KEY_STORE_NAME, name);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    private void updateCurrentStoreLabel(String name) {
        tvCurrentStore.setText(TextUtils.isEmpty(name)
                ? "尚未設定門市名稱"
                : "目前門市：" + name);
    }

    @SuppressLint("BatteryLife")
    private void checkBatteryOptimization() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return;
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        if (pm == null) return;
        if (pm.isIgnoringBatteryOptimizations(getPackageName())) return;

        new AlertDialog.Builder(this)
                .setTitle("建議設定：停用電池優化")
                .setMessage("為確保通知不被系統中斷，建議將此 App 排除在電池優化之外。\n\n點「前往設定」→ 找到「門市通知」→ 選「不限制」。")
                .setPositiveButton("前往設定", (d, w) -> {
                    Intent i = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                    i.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(i);
                })
                .setNegativeButton("稍後再說", null)
                .show();
    }
}
