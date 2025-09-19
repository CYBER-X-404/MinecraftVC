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

import io.agora.rtc2.Constants;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;
import io.agora.rtc2.ChannelMediaOptions;

public class MainActivity extends AppCompatActivity {

    private RtcEngine agoraEngine;
    private final String appId = "1f3aba7f3dea4d30a53b0a77317e3c83"; 
    private final String channelName = "minecraft-vc-channel-1";
    private final int uid = 0;

    private Button toggleButton;
    private boolean isVoiceChatActive = false;
    private static final int PERMISSION_REQ_ID = 22;
    private static final String[] REQUESTED_PERMISSIONS = {Manifest.permission.RECORD_AUDIO};
    private static final int CODE_DRAW_OVER_OTHER_APP_PERMISSION = 2084;

    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        @Override
        public void onJoinChannelSuccess(final String channel, final int uid, int elapsed) {
            runOnUiThread(new Runnable() { @Override public void run() { Toast.makeText(getApplicationContext(), "Joined Channel!", Toast.LENGTH_SHORT).show(); } });
        }
        @Override
        public void onUserJoined(final int uid, int elapsed) {
            runOnUiThread(new Runnable() { @Override public void run() { Toast.makeText(getApplicationContext(), "Friend joined!", Toast.LENGTH_SHORT).show(); } });
        }
        @Override
        public void onUserOffline(final int uid, int reason) {
            runOnUiThread(new Runnable() { @Override public void run() { Toast.makeText(getApplicationContext(), "Friend left.", Toast.LENGTH_SHORT).show(); } });
        }
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        toggleButton = findViewById(R.id.toggleButton);
        setupUI();
        
        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isVoiceChatActive) {
                    if (!checkSelfPermission()) {
                        ActivityCompat.requestPermissions(MainActivity.this, REQUESTED_PERMISSIONS, PERMISSION_REQ_ID);
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(MainActivity.this)) {
                        checkOverlayPermission();
                    } else {
                        startVoiceChat();
                    }
                } else {
                    stopVoiceChat();
                }
            }
        });
    }

    private void initializeAndJoinChannel() {
        try {
            RtcEngineConfig config = new RtcEngineConfig();
            config.mContext = getBaseContext();
            config.mAppId = appId;
            config.mEventHandler = mRtcEventHandler;
            agoraEngine = RtcEngine.create(config);
            ChannelMediaOptions options = new ChannelMediaOptions();
            options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER;
            options.channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION;
            agoraEngine.joinChannel(null, channelName, uid, options);
        } catch (Exception e) {
            Toast.makeText(this, "Error initializing Agora: " + e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    private void setupUI() { 
        try {
            // NOTE: You need to manually add the font to 'app/src/main/assets/fonts/minecraft_font.ttf'
            Typeface minecraftFont = Typeface.createFromAsset(getAssets(), "fonts/minecraft_font.ttf");
            toggleButton.setTypeface(minecraftFont);
        } catch (Exception e) { 
            // This will fail if font is not present, which is okay for now.
        }
        updateButtonState();
    }
    private boolean checkSelfPermission() { return ContextCompat.checkSelfPermission(this, REQUESTED_PERMISSIONS[0]) == PackageManager.PERMISSION_GRANTED; }
    private void startVoiceChat() { 
        isVoiceChatActive = true;
        updateButtonState();
        initializeAndJoinChannel();
        startService(new Intent(this, VoiceChatService.class));
    }
    private void stopVoiceChat() { 
        isVoiceChatActive = false;
        updateButtonState();
        if (agoraEngine != null) { agoraEngine.leaveChannel(); }
        stopService(new Intent(this, VoiceChatService.class));
    }
    @Override
    protected void onDestroy() { 
        super.onDestroy();
        if (agoraEngine != null) { RtcEngine.destroy(); }
    }
    private void updateButtonState() { 
        if (isVoiceChatActive) {
            toggleButton.setText("OFF");
            ((GradientDrawable) toggleButton.getBackground()).setColor(ContextCompat.getColor(this, R.color.mc_dirt_brown));
        } else {
            toggleButton.setText("START");
            ((GradientDrawable) toggleButton.getBackground()).setColor(ContextCompat.getColor(this, R.color.mc_stone_grey));
        }
    }
    @Override
    public void onRequestPermissionsResult(int r, @NonNull String[] p, @NonNull int[] g) { 
        super.onRequestPermissionsResult(r, p, g);
        if (r == PERMISSION_REQ_ID && g.length > 0 && g[0] == PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) { checkOverlayPermission(); } 
            else { startVoiceChat(); }
        } else { Toast.makeText(this, "Microphone permission denied", Toast.LENGTH_SHORT).show(); }
    }
    private void checkOverlayPermission() { 
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, CODE_DRAW_OVER_OTHER_APP_PERMISSION);
    }
    @Override
    protected void onActivityResult(int r, int res, @Nullable Intent d) { 
        super.onActivityResult(r, res, d);
        if (r == CODE_DRAW_OVER_OTHER_APP_PERMISSION && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) {
            startVoiceChat();
        } else { Toast.makeText(this, "Draw over other apps permission denied", Toast.LENGTH_SHORT).show(); }
    }
}
