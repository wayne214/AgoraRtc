package com.example.fantuan.agora_rc;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import io.agora.rtc.Constants;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;
import io.agora.rtc.video.VideoEncoderConfiguration;

public class BroadcasterActivity extends AppCompatActivity {
    private static final String LOG_TAG = BroadcasterActivity.class.getSimpleName();

    private RtcEngine mRtcEngine;
    private FrameLayout mFlCam;
    private FrameLayout mFlSS;
    private boolean mSS = false;
    private VideoEncoderConfiguration mVEC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_broadcaster);

        mFlCam = (FrameLayout) findViewById(R.id.camera_preview);
        mFlSS = (FrameLayout) findViewById(R.id.screen_share_preview);

        initAgoraEngineAndJoinChannel();

    }

    private void initializeAgoraEngine() {
        try {
            mRtcEngine = RtcEngine.create(getApplicationContext(), getString(R.string.agora_app_id), null);
        } catch (Exception e) {
            Log.e(LOG_TAG, Log.getStackTraceString(e));

            throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
        }
    }

    private void initAgoraEngineAndJoinChannel() {
//        initializeAgoraEngine();
        setupVideoProfile();
        setupLocalVideo();
        joinChannel();
    }

    public void onCameraSharingClicked(View view) {
        Button button = (Button) view;
        if (button.isSelected()) {
            button.setSelected(false);
            button.setText(getResources().getString(R.string.label_start_camera));
        } else {
            button.setSelected(true);
            button.setText(getResources().getString(R.string.label_stop_camera));
        }

        mRtcEngine.enableLocalVideo(button.isSelected());
    }

    private void setupVideoProfile() {
        mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);
        mRtcEngine.enableVideo();
        mVEC = new VideoEncoderConfiguration(VideoEncoderConfiguration.VD_640x360,
                VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                VideoEncoderConfiguration.STANDARD_BITRATE,
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT);
        mRtcEngine.setVideoEncoderConfiguration(mVEC);
        mRtcEngine.setClientRole(Constants.CLIENT_ROLE_BROADCASTER);
    }

    private void setupLocalVideo() {
        SurfaceView camV = RtcEngine.CreateRendererView(getApplicationContext());
        camV.setZOrderOnTop(true);
        camV.setZOrderMediaOverlay(true);
        mFlCam.addView(camV, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        mRtcEngine.setupLocalVideo(new VideoCanvas(camV, VideoCanvas.RENDER_MODE_FIT, Constant.CAMERA_UID));
        mRtcEngine.enableLocalVideo(false);
    }

    private void setupRemoteView(int uid) {
        SurfaceView ssV = RtcEngine.CreateRendererView(getApplicationContext());
        ssV.setZOrderOnTop(true);
        ssV.setZOrderMediaOverlay(true);
        mFlSS.addView(ssV, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mRtcEngine.setupRemoteVideo(new VideoCanvas(ssV, VideoCanvas.RENDER_MODE_FIT, uid));
    }


    private void joinChannel() {
        mRtcEngine.joinChannel(null, getResources().getString(R.string.label_channel_name),"Extra Optional Data", Constant.CAMERA_UID); // if you do not specify the uid, we will generate the uid for you
    }

    private void leaveChannel() {
        mRtcEngine.leaveChannel();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        leaveChannel();
        RtcEngine.destroy();
        mRtcEngine = null;
        if (mSS) {
//            mSSClient.stop(getApplicationContext());
        }
    }
}
