package com.minecraft.vc;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import io.agora.rtc2.ChannelMediaOptions;
import io.agora.rtc2.Constants;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;

public class AgoraManager {

    private static RtcEngine agoraEngine;
    private static boolean isMicMuted = false;

    private static final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        @Override
        public void onJoinChannelSuccess(final String channel, final int uid, int elapsed) {
            // Toast on the main thread
            new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(App.getContext(), "Channel Joined!", Toast.LENGTH_SHORT).show());
        }
        @Override
        public void onUserJoined(final int uid, int elapsed) {
            new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(App.getContext(), "Friend joined!", Toast.LENGTH_SHORT).show());
        }
        @Override
        public void onUserOffline(final int uid, int reason) {
            new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(App.getContext(), "Friend left.", Toast.LENGTH_SHORT).show());
        }
    };

    public static synchronized void initialize(Context context, String appId, String channelName) {
        if (agoraEngine != null) {
            return; // Already initialized
        }
        try {
            RtcEngineConfig config = new RtcEngineConfig();
            config.mContext = context;
            config.mAppId = appId;
            config.mEventHandler = mRtcEventHandler;
            agoraEngine = RtcEngine.create(config);

            ChannelMediaOptions options = new ChannelMediaOptions();
            options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER;
            options.channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION;
            agoraEngine.joinChannel(null, channelName, 0, options);
            isMicMuted = false;
            agoraEngine.muteLocalAudioStream(false);

        } catch (Exception e) {
            new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(App.getContext(), "Agora Init Failed: " + e.toString(), Toast.LENGTH_LONG).show());
        }
    }

    public static synchronized void destroy() {
        if (agoraEngine != null) {
            agoraEngine.leaveChannel();
            RtcEngine.destroy();
            agoraEngine = null;
        }
    }

    public static synchronized void toggleMute() {
        if (agoraEngine == null) return;
        
        isMicMuted = !isMicMuted;
        agoraEngine.muteLocalAudioStream(isMicMuted);

        String message = isMicMuted ? "Microphone OFF" : "Microphone ON";
        new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(App.getContext(), message, Toast.LENGTH_SHORT).show());
    }
    
    public static boolean isMicMuted() {
        return isMicMuted;
    }
}
