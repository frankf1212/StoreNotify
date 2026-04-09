# 門市通知 App — StoreNotify

## 功能說明
- 在 Android 狀態列（通知欄）常駐顯示門市名稱與當日日期
- 格式：標題 = 門市名稱，內容 = 2025/06/10 (週二)
- 重開機後自動恢復通知
- 日期每天午夜自動更新

## 安裝方式（需 Android Studio）

### 環境需求
- Android Studio Hedgehog (2023.1) 以上
- JDK 17
- Android SDK 34

### 步驟
1. 開啟 Android Studio → File → Open → 選擇此資料夾（StoreNotify）
2. 等待 Gradle sync 完成
3. 連接 PDA（開啟 USB 偵錯模式）
4. 點選 Run ▶ 即可安裝

### 打包 APK（免 USB，可傳送安裝）
Build → Build Bundle(s) / APK(s) → Build APK(s)
APK 位置：app/build/outputs/apk/debug/app-debug.apk

## App 使用方式
1. 開啟「門市通知」App
2. 輸入門市名稱（例如：台中文心店）
3. 點「儲存並啟動通知」
4. 狀態列即出現常駐通知
5. 之後重開機會自動恢復，不需再手動啟動

## 修改門市名稱
重新開啟 App → 修改名稱 → 點「儲存並啟動通知」

## 注意事項
- Android 13 以上（API 33+）首次啟動時會詢問通知權限，請點「允許」
- 部分廠牌 PDA 可能需在「電池優化」中將此 App 設為「不限制」，以避免背景被強制終止

## 專案結構
```
StoreNotify/
├── app/src/main/
│   ├── AndroidManifest.xml
│   ├── java/com/shopee/storenotify/
│   │   ├── MainActivity.java          主畫面
│   │   ├── NotificationService.java   常駐通知服務
│   │   ├── BootReceiver.java          開機自動啟動
│   │   └── DateUpdateReceiver.java    日期變更監聽
│   └── res/
│       ├── layout/activity_main.xml
│       ├── drawable/ic_store.xml
│       └── values/{strings,styles}.xml
├── build.gradle
├── settings.gradle
└── gradle.properties
```
