package com.sajib.neckbandpopup;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import androidx.core.app.NotificationCompat;

public class BLEScanService extends Service {

    private static final String TAG = "BLEScanService";
    private static final String CHANNEL_ID = "neckband_scan_channel";
    private static final int NOTIFICATION_ID = 1;

    // Your neckband's MAC address
    private static final String TARGET_MAC = "88:0E:85:7F:9F:4A";

    // Cooldown so popup doesn't spam (30 seconds)
    private static final long POPUP_COOLDOWN_MS = 30000;

    private BluetoothLeScanner bleScanner;
    private boolean isScanning = false;
    private long lastPopupTime = 0;
    private Handler handler = new Handler(Looper.getMainLooper());

    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            String deviceAddress = result.getDevice().getAddress();
            Log.d(TAG, "Found BLE device: " + deviceAddress);

            if (TARGET_MAC.equalsIgnoreCase(deviceAddress)) {
                Log.d(TAG, "🎯 TARGET NECKBAND FOUND!");
                long now = System.currentTimeMillis();

                // Only show popup if cooldown has passed
                if (now - lastPopupTime > POPUP_COOLDOWN_MS) {
                    lastPopupTime = now;
                    showPopup();
                }
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e(TAG, "BLE Scan failed with error: " + errorCode);
            // Retry scan after 5 seconds
            handler.postDelayed(() -> startBLEScan(), 5000);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && "STOP".equals(intent.getAction())) {
            stopBLEScan();
            stopForeground(true);
            stopSelf();
            return START_NOT_STICKY;
        }

        // Start as foreground service with notification
        startForeground(NOTIFICATION_ID, buildNotification());
        startBLEScan();

        return START_STICKY; // Restart if killed by system
    }

    private void startBLEScan() {
        BluetoothManager btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (btManager == null) return;

        BluetoothAdapter btAdapter = btManager.getAdapter();
        if (btAdapter == null) return;

        // BLE scanning works even when Bluetooth is "off" in quick settings
        bleScanner = btAdapter.getBluetoothLeScanner();
        if (bleScanner == null) {
            // Retry after 3 seconds
            handler.postDelayed(this::startBLEScan, 3000);
            return;
        }

        // Use LOW_LATENCY for fastest detection
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();

        bleScanner.startScan(null, settings, scanCallback);
        isScanning = true;
        Log.d(TAG, "BLE Scanning started - watching for " + TARGET_MAC);
    }

    private void stopBLEScan() {
        if (bleScanner != null && isScanning) {
            bleScanner.stopScan(scanCallback);
            isScanning = false;
            Log.d(TAG, "BLE Scanning stopped");
        }
    }

    private void showPopup() {
        Log.d(TAG, "Showing popup!");
        Intent popupIntent = new Intent(this, PopupActivity.class);
        popupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        popupIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(popupIntent);
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Neckband Scanner",
                NotificationManager.IMPORTANCE_LOW
        );
        channel.setDescription("Scanning for your neckband in background");
        channel.setShowBadge(false);

        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }

    private Notification buildNotification() {
        Intent mainIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, mainIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Neckband Popup")
                .setContentText("Watching for your realme Buds Wireless 5 ANC...")
                .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setSilent(true)
                .build();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopBLEScan();
        handler.removeCallbacksAndMessages(null);
    }
}
