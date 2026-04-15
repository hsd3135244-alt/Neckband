package com.sajib.neckbandpopup;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;

public class PopupActivity extends AppCompatActivity {

    // Realme Buds Wireless 5 ANC Dawn Silver image URL
    private static final String NECKBAND_IMAGE_URL =
            "https://image01.realme.net/general/20240805/1722842712584.png";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Make window appear over lockscreen
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        );

        setContentView(R.layout.activity_popup);

        ImageView deviceImage = findViewById(R.id.deviceImage);
        TextView deviceName = findViewById(R.id.deviceName);
        TextView deviceDesc = findViewById(R.id.deviceDesc);
        Button closeBtn = findViewById(R.id.closeBtn);
        Button connectBtn = findViewById(R.id.connectBtn);

        // Set device info
        deviceName.setText("Sajib's realme Buds Wireless 5 ANC");
        deviceDesc.setText("realme Buds Wireless 5 ANC will appear on devices linked with your Google account.");

        // Load neckband image using Glide
        Glide.with(this)
                .load(NECKBAND_IMAGE_URL)
                .placeholder(android.R.drawable.ic_menu_compass)
                .into(deviceImage);

        // Close button
        closeBtn.setOnClickListener(v -> finish());

        // Connect button - opens Bluetooth enable dialog (Android 13+)
        connectBtn.setOnClickListener(v -> {
            enableBluetooth();
            finish();
        });

        // Tap outside to close
        findViewById(R.id.rootLayout).setOnClickListener(v -> finish());
        // But don't close when tapping the card itself
        findViewById(R.id.cardLayout).setOnClickListener(v -> {});
    }

    private void enableBluetooth() {
        BluetoothManager btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (btManager == null) return;

        BluetoothAdapter btAdapter = btManager.getAdapter();
        if (btAdapter == null) return;

        if (!btAdapter.isEnabled()) {
            // Android 13+ - show system dialog to enable Bluetooth
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            enableBtIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(enableBtIntent);
        }
        // If already enabled, just close - it will auto-connect
    }
}
