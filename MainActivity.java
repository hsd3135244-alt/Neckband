package com.sajib.neckbandpopup;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView statusText = findViewById(R.id.statusText);
        Button startBtn = findViewById(R.id.startBtn);
        Button stopBtn = findViewById(R.id.stopBtn);

        // Check overlay permission first
        if (!Settings.canDrawOverlays(this)) {
            statusText.setText("⚠️ Please grant 'Display over other apps' permission first!");
            startBtn.setEnabled(false);

            Button permBtn = findViewById(R.id.permBtn);
            permBtn.setVisibility(android.view.View.VISIBLE);
            permBtn.setOnClickListener(v -> {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            });
        } else {
            statusText.setText("✅ Service is ready! Tap START to begin scanning.");
        }

        startBtn.setOnClickListener(v -> {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Please grant overlay permission first!", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent serviceIntent = new Intent(this, BLEScanService.class);
            serviceIntent.setAction("START");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
            statusText.setText("🔍 Scanning for your neckband...\n\nService is running in background.\nYou can close this app now.");
            Toast.makeText(this, "Scanning started!", Toast.LENGTH_SHORT).show();
        });

        stopBtn.setOnClickListener(v -> {
            Intent serviceIntent = new Intent(this, BLEScanService.class);
            serviceIntent.setAction("STOP");
            startService(serviceIntent);
            statusText.setText("⛔ Service stopped.");
            Toast.makeText(this, "Scanning stopped!", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh UI when returning from permission screen
        TextView statusText = findViewById(R.id.statusText);
        Button startBtn = findViewById(R.id.startBtn);
        Button permBtn = findViewById(R.id.permBtn);

        if (Settings.canDrawOverlays(this)) {
            statusText.setText("✅ Service is ready! Tap START to begin scanning.");
            startBtn.setEnabled(true);
            permBtn.setVisibility(android.view.View.GONE);
        }
    }
}
