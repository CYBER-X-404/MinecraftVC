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
import android.widget.Toast;

public class VoiceChatService extends Service {

    private WindowManager mWindowManager;
    private View mFloatingView;
    private WindowManager.LayoutParams params;
    private boolean isVoiceChatOn = true;

    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            mFloatingView = LayoutInflater.from(this).inflate(R.layout.layout_floating_widget, null);

            params = new WindowManager.LayoutParams(
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
                private int initialX, initialY;
                private float initialTouchX, initialTouchY;
                private long touchStartTime;
                private static final int CLICK_ACTION_THRESHOLD = 200;
                private static final float CLICK_DRAG_THRESHOLD = 10;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            initialX = params.x;
                            initialY = params.y;
                            initialTouchX = event.getRawX();
                            initialTouchY = event.getRawY();
                            touchStartTime = System.currentTimeMillis();
                            return true;

                        case MotionEvent.ACTION_MOVE:
                            params.x = initialX + (int) (event.getRawX() - initialTouchX);
                            params.y = initialY + (int) (event.getRawY() - initialTouchY);
                            mWindowManager.updateViewLayout(mFloatingView, params);
                            return true;

                        case MotionEvent.ACTION_UP:
                            long touchDuration = System.currentTimeMillis() - touchStartTime;
                            float xDiff = Math.abs(event.getRawX() - initialTouchX);
                            float yDiff = Math.abs(event.getRawY() - initialTouchY);

                            if (touchDuration < CLICK_ACTION_THRESHOLD && xDiff < CLICK_DRAG_THRESHOLD && yDiff < CLICK_DRAG_THRESHOLD) {
                                isVoiceChatOn = !isVoiceChatOn;
                                if (isVoiceChatOn) {
                                    mFloatingView.setAlpha(1.0f);
                                    Toast.makeText(getApplicationContext(), "Microphone ON", Toast.LENGTH_SHORT).show();
                                } else {
                                    mFloatingView.setAlpha(0.6f);
                                    Toast.makeText(getApplicationContext(), "Microphone OFF", Toast.LENGTH_SHORT).show();
                                }
                            }
                            return true;
                    }
                    return false;
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Floating Icon Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFloatingView != null) try { mWindowManager.removeView(mFloatingView); } catch (Exception e) {}
    }
}
