package com.minecraft.vc;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private Button toggleButton;
    private static boolean isServiceRunning = false;
    private static final int PERMISSION_REQ_ID = 22;
    private static final String[] REQUESTED_PERMISSIONS = {Manifest.permission.RECORD_AUDIO};
    private static final int CODE_DRAW_OVER_OTHER_APP_PERMISSION = 2084;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        toggleButton = findViewById(R.id.toggleButton);
        setupUI();
        
        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isServiceRunning) {
                    if (!checkSelfPermission()) {
                        ActivityCompat.requestPermissions(MainActivity.this, REQUESTED_PERMISSIONS, PERMISSION_REQ_ID);
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(MainActivity.this)) {
                        checkOverlayPermission();
                    } else {
                        startVoiceChatService();
                    }
                } else {
                    stopVoiceChatService();
                }
            }
        });
    }

    private void startVoiceChatService() { 
        isServiceRunning = true;
        updateButtonState();
        startService(new Intent(this, VoiceChatService.class));
    }
    
    private void stopVoiceChatService() { 
        isServiceRunning = false;
        updateButtonState();
        stopService(new Intent(this, VoiceChatService.class));
    }

    private void setupUI() { 
        try {
            Typeface minecraftFont = Typeface.createFromAsset(getAssets(), "fonts/minecraft_font.ttf");
            toggleButton.setTypeface(minecraftFont);
        } catch (Exception e) { 
            Toast.makeText(this, "Font not found!", Toast.LENGTH_SHORT).show(); 
        }
        updateButtonState();
    }
    
    private void updateButtonState() { 
        if (isServiceRunning) {
            toggleButton.setText("OFF");
            toggleButton.setBackgroundColor(ContextCompat.getColor(this, R.color.mc_dirt_brown));
        } else {
            toggleButton.setText("START");
            toggleButton.setBackgroundColor(ContextCompat.getColor(this, R.color.mc_stone_grey));
        }
    }
    
    private boolean checkSelfPermission() { 
        return ContextCompat.checkSelfPermission(this, REQUESTED_PERMISSIONS[0]) == PackageManager.PERMISSION_GRANTED; 
    }
    
    @Override
    public void onRequestPermissionsResult(int r, @NonNull String[] p, @NonNull int[] g) { 
        super.onRequestPermissionsResult(r, p, g);
        if (r == PERMISSION_REQ_ID && g.length > 0 && g[0] == PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) { 
                checkOverlayPermission(); 
            } else { 
                startVoiceChatService(); 
            }
        } else { 
            Toast.makeText(this, "Microphone permission denied", Toast.LENGTH_SHORT).show(); 
        }
    }
    
    private void checkOverlayPermission() { 
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, CODE_DRAW_OVER_OTHER_APP_PERMISSION);
    }
    
    @Override
    protected void onActivityResult(int r, int res, @Nullable Intent d) { 
        super.onActivityResult(r, res, d);
        if (r == CODE_DRAW_OVER_OTHER_APP_PERMISSION && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) {
            startVoiceChatService();
        } else { 
            Toast.makeText(this, "Draw over other apps permission denied", Toast.LENGTH_SHORT).show(); 
        }
    }
}
