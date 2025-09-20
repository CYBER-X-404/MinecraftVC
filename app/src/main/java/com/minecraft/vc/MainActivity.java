package com.minecraft.vc;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

public class VoiceChatService extends Service {

    private WindowManager mWindowManager;
    private View mFloatingView;
    private final String appId = "1f3aba7f3dea4d30a53b0a77317e3c83";
    private final String channelName = "minecraft-vc-channel-1";

    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        AgoraManager.initialize(getApplicationContext(), appId, channelName);
        setupFloatingView();
        return START_NOT_STICKY;
    }

    private void setupFloatingView() {
        mFloatingView = LayoutInflater.from(this).inflate(R.layout.layout_floating_widget, null);
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams( /* ...Ager Motoi... */ );
        
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(mFloatingView, params);

        mFloatingView.findViewById(R.id.root_container).setOnTouchListener(new View.OnTouchListener() {
            // ... (Dragger Code Ager Motoi Thik Ache) ...
            
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // ...
                if (/* ...Clicker Sharthe Pure Hole... */) {
                    AgoraManager.toggleMute();
                    if (AgoraManager.isMicMuted()) {
                        mFloatingView.setAlpha(0.6f);
                    } else {
                        mFloatingView.setAlpha(1.0f);
                    }
                }
                return true;
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mWindowManager != null && mFloatingView != null) {
            mWindowManager.removeView(mFloatingView);
        }
        AgoraManager.destroy();
    }
}
