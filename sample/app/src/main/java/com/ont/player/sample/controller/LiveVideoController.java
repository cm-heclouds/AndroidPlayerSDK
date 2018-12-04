package com.ont.player.sample.controller;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ont.player.sample.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by betali on 2018/5/2.
 */

public class LiveVideoController extends StandardVideoController {

    public static final int PERMISSIONS_REQUEST = 8954;
    private TextView audioButton;

    private boolean pushingAudio;

    public LiveVideoController(@NonNull Context context) {

        super(context);
    }

    public LiveVideoController(@NonNull Context context, @Nullable AttributeSet attrs) {

        super(context, attrs);
    }

    public LiveVideoController(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {

        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void initView() {

        super.initView();

        // 隐藏时间等view
        bottomProgress.setVisibility(GONE);
        videoProgress.setVisibility(INVISIBLE);
        totalTime.setVisibility(INVISIBLE);

        setLive();
    }

    @Override
    public void onClick(View v) {

        int i = v.getId();
        if (i == R.id.mic_audio) {

            opPushAudio(!pushingAudio);
        } else {

            super.onClick(v);
        }
    }

    @Override
    public void onSupportPushAudio(boolean support) {

        if (support) {
            // 语音推送按钮
            audioButton = controllerView.findViewById(R.id.mic_audio);
            audioButton.setVisibility(VISIBLE);
            audioButton.setOnClickListener(this);
        }
    }

    @Override
    public void onEnablePushAudio(boolean enable) {

        audioButton.setEnabled(enable);
    }

    @Override
    public void onStoppedPushAudio() {

        pushingAudio = false;
        audioButton.setText("录音");
    }

    @Override
    public void opPushAudio(boolean start) {

        if (start && !pushingAudio) {

            if (isRecordPermissionGranted()) {

                if (mVideoView.startPushAudio() == 0) {

                    pushingAudio = true;
                    audioButton.setText("停止");
                } else {
                    Toast.makeText(getContext(), getContext().getString(R.string.start_audio_fail), Toast.LENGTH_SHORT).show();
                }
            } else {

                requestRecordPermission();
            }
        } else if (!start && pushingAudio) {

            pushingAudio = false;
            mVideoView.stopPushAudio();
            audioButton.setText("录音");
        }
    }

    private boolean isRecordPermissionGranted() {

        boolean microPhonePermissionGranted = ContextCompat.checkSelfPermission(hostPage, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED;

        return microPhonePermissionGranted;
    }

    private void requestRecordPermission() {

        boolean microPhonePermissionGranted = ContextCompat.checkSelfPermission(hostPage, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED;

        final List<String> permissionList = new ArrayList();
        if (!microPhonePermissionGranted) {
            permissionList.add(Manifest.permission.RECORD_AUDIO);
        }
        if (permissionList.size() > 0 )
        {
            String[] permissionArray = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(hostPage, permissionArray, PERMISSIONS_REQUEST);
        }
    }
}
