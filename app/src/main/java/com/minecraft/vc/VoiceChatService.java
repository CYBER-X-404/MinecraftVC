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
        setupFloatingView();
        return START_NOT_STICKY;
    }

    private void setupFloatingView() {
        if (mFloatingView != null) return; // Jate bar bar toiri na hoy

        mFloatingView = LayoutInflater.from(this).inflate(R.layout.layout_floating_widget, null);
        
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = 100;
        
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(mFloatingView, params);

        mFloatingView.findViewById(R.id.root_container).setOnTouchListener(new View.OnTouchListener() {
            private long touchStartTime;
            
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    touchStartTime = System.currentTimeMillis();
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (System.currentTimeMillis() - touchStartTime < 200) {
                        // --- APNAR LOGIC EKHANE ---
                        AgoraManager.toggleConnection(getApplicationContext(), appId, channelName);
                        
                        if (AgoraManager.isConnected()) {
                            mFloatingView.setAlpha(1.0f); // Ujjol
                        } else {
                            mFloatingView.setAlpha(0.6f); // Apsha
                        }
                    }
                }
                // Drag code ekhane dewa jete pare, kintu age click thik hok
                return true;
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mWindowManager != null && mFloatingView != null) {
            mWindowManager.removeView(mFloatingView);
            mFloatingView = null;
        }
        AgoraManager.destroy();
    }
}
