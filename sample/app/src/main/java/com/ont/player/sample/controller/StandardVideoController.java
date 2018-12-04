package com.ont.player.sample.controller;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ont.media.player.IjkVideoView;
import com.ont.media.player.controller.GestureVideoController;
import com.ont.media.player.util.BatteryReceiver;
import com.ont.media.player.util.L;
import com.ont.media.player.util.WindowUtil;
import com.ont.media.player.widget.MarqueeTextView;
import com.ont.player.sample.PlayerActivity;
import com.ont.player.sample.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 直播/点播控制器
 * Created by Devlin_n on 2017/4/7.
 */

public class StandardVideoController extends GestureVideoController implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {
    protected TextView totalTime, currTime;
    protected ImageView fullScreenButton;
    protected LinearLayout bottomContainer, topContainer;
    protected SeekBar videoProgress;
    protected ImageView backButton;
    protected ImageView lock;
    protected MarqueeTextView title;
    private boolean isLive;
    private boolean isDragging;

    protected ProgressBar bottomProgress;
    private ImageView playButton;
    private ImageView startPlayButton;
    private ProgressBar loadingProgress;
    private ImageView thumb;
    private LinearLayout completeContainer;
    private TextView sysTime;//系统当前时间
    private ImageView batteryLevel;//电量
    private Animation showAnim = AnimationUtils.loadAnimation(getContext(), R.anim.dkplayer_anim_alpha_in);
    private Animation hideAnim = AnimationUtils.loadAnimation(getContext(), R.anim.dkplayer_anim_alpha_out);
    private BatteryReceiver mBatteryReceiver;
    protected ImageView refresh;

    protected PlayerActivity hostPage;
    private TextView screenshotButton;
    protected String deviceId;
    protected String channelId;
    protected String apiKey;

    public StandardVideoController(@NonNull Context context) {
        this(context, null);
    }

    public StandardVideoController(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StandardVideoController(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setHostPage(PlayerActivity hostPage) {
        this.hostPage = hostPage;
    }

    public void setApiKey(String apiKey) {

        this.apiKey = apiKey;
    }

    public void setDeviceId(String deviceId) {

        this.deviceId = deviceId;
    }

    public void setChannelId(String channelId) {

        this.channelId = channelId;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dkplayer_layout_standard_controller;
    }

    @Override
    protected void initView() {
        super.initView();
        fullScreenButton = controllerView.findViewById(R.id.fullscreen);
        fullScreenButton.setOnClickListener(this);
        bottomContainer = controllerView.findViewById(R.id.bottom_container);
        topContainer = controllerView.findViewById(R.id.top_container);
        videoProgress = controllerView.findViewById(R.id.seekBar);
        videoProgress.setOnSeekBarChangeListener(this);
        totalTime = controllerView.findViewById(R.id.total_time);
        currTime = controllerView.findViewById(R.id.curr_time);
        backButton = controllerView.findViewById(R.id.back);
        backButton.setOnClickListener(this);
        lock = controllerView.findViewById(R.id.lock);
        lock.setOnClickListener(this);
        thumb = controllerView.findViewById(R.id.thumb);
        thumb.setOnClickListener(this);
        playButton = controllerView.findViewById(R.id.iv_play);
        playButton.setOnClickListener(this);
        startPlayButton = controllerView.findViewById(R.id.start_play);
        loadingProgress = controllerView.findViewById(R.id.loading);
        bottomProgress = controllerView.findViewById(R.id.bottom_progress);
        ImageView rePlayButton = controllerView.findViewById(R.id.iv_replay);
        rePlayButton.setOnClickListener(this);
        completeContainer = controllerView.findViewById(R.id.complete_container);
        completeContainer.setOnClickListener(this);
        title = controllerView.findViewById(R.id.title);
        sysTime = controllerView.findViewById(R.id.sys_time);
        batteryLevel = controllerView.findViewById(R.id.iv_battery);
        mBatteryReceiver = new BatteryReceiver(batteryLevel);
        refresh = controllerView.findViewById(R.id.iv_refresh);
        refresh.setOnClickListener(this);
        screenshotButton = controllerView.findViewById(R.id.screen_shot);
        screenshotButton.setOnClickListener(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getContext().unregisterReceiver(mBatteryReceiver);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getContext().registerReceiver(mBatteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.fullscreen || i == R.id.back) {
            doStartStopFullScreen();
        } else if (i == R.id.lock) {
            doLockUnlock();
        } else if (i == R.id.iv_play || i == R.id.thumb) {
            doPauseResume();
        } else if (i == R.id.iv_replay) {
            mVideoView.retry();
        } else if (i == R.id.iv_refresh) {
            mVideoView.refresh();
        } else if (i == R.id.screen_shot) {

            if (isSDCardPermissionGranted()) {

                doScreenshot();
                // TODO betali
                screenshotButton.setEnabled(false);
            } else {

                requestSDCardPermission();
            }
        }
    }

    public void showTitle() {
        title.setVisibility(View.VISIBLE);
    }

    @Override
    public void setPlayerState(int playerState) {
        switch (playerState) {
            case IjkVideoView.PLAYER_NORMAL:
                L.e("PLAYER_NORMAL");
                if (isLocked) return;
                setLayoutParams(new LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
                gestureEnabled = false;
                fullScreenButton.setSelected(false);
                backButton.setVisibility(View.GONE);
                lock.setVisibility(View.GONE);
                title.setVisibility(View.INVISIBLE);
                sysTime.setVisibility(View.GONE);
                batteryLevel.setVisibility(View.GONE);
                topContainer.setVisibility(View.GONE);
                break;
            case IjkVideoView.PLAYER_FULL_SCREEN:
                L.e("PLAYER_FULL_SCREEN");
                if (isLocked) return;
                gestureEnabled = true;
                fullScreenButton.setSelected(true);
                backButton.setVisibility(View.VISIBLE);
                title.setVisibility(View.VISIBLE);
                sysTime.setVisibility(View.VISIBLE);
                batteryLevel.setVisibility(View.VISIBLE);
                if (mShowing) {
                    lock.setVisibility(View.VISIBLE);
                    topContainer.setVisibility(View.VISIBLE);
                } else {
                    lock.setVisibility(View.GONE);
                }
                break;
        }
    }

    @Override
    public void setPlayState(int playState) {
        super.setPlayState(playState);
        switch (playState) {
            case IjkVideoView.STATE_IDLE:
                L.e("STATE_IDLE");
                hide();
                isLocked = false;
                lock.setSelected(false);
                mVideoView.setLock(false);
                bottomProgress.setProgress(0);
                bottomProgress.setSecondaryProgress(0);
                videoProgress.setProgress(0);
                videoProgress.setSecondaryProgress(0);
                completeContainer.setVisibility(View.GONE);
                bottomProgress.setVisibility(View.GONE);
                loadingProgress.setVisibility(View.GONE);
                startPlayButton.setVisibility(View.VISIBLE);
                thumb.setVisibility(View.VISIBLE);
                break;
            case IjkVideoView.STATE_PLAYING:
                L.e("STATE_PLAYING");
                post(mShowProgress);
                playButton.setSelected(true);
                loadingProgress.setVisibility(View.GONE);
                completeContainer.setVisibility(View.GONE);
                thumb.setVisibility(View.GONE);
                startPlayButton.setVisibility(View.GONE);
                break;
            case IjkVideoView.STATE_PAUSED:
                L.e("STATE_PAUSED");
                playButton.setSelected(false);
                startPlayButton.setVisibility(View.GONE);
                break;
            case IjkVideoView.STATE_PREPARING:
                L.e("STATE_PREPARING");
                completeContainer.setVisibility(View.GONE);
                startPlayButton.setVisibility(View.GONE);
                loadingProgress.setVisibility(View.VISIBLE);
//                thumb.setVisibility(View.VISIBLE);
                break;
            case IjkVideoView.STATE_PREPARED:
                L.e("STATE_PREPARED");
                if (!isLive) bottomProgress.setVisibility(View.VISIBLE);
//                loadingProgress.setVisibility(GONE);
                startPlayButton.setVisibility(View.GONE);
                break;
            case IjkVideoView.STATE_ERROR:
                L.e("STATE_ERROR");
                startPlayButton.setVisibility(View.GONE);
                loadingProgress.setVisibility(View.GONE);
                thumb.setVisibility(View.GONE);
                bottomProgress.setVisibility(View.GONE);
                topContainer.setVisibility(View.GONE);
                break;
            case IjkVideoView.STATE_BUFFERING:
                L.e("STATE_BUFFERING");
                startPlayButton.setVisibility(View.GONE);
                loadingProgress.setVisibility(View.VISIBLE);
                thumb.setVisibility(View.GONE);
                break;
            case IjkVideoView.STATE_BUFFERED:
                loadingProgress.setVisibility(View.GONE);
                startPlayButton.setVisibility(View.GONE);
                thumb.setVisibility(View.GONE);
                L.e("STATE_BUFFERED");
                break;
            case IjkVideoView.STATE_PLAYBACK_COMPLETED:
                L.e("STATE_PLAYBACK_COMPLETED");
                hide();
                removeCallbacks(mShowProgress);
                startPlayButton.setVisibility(View.GONE);
                thumb.setVisibility(View.VISIBLE);
                completeContainer.setVisibility(View.VISIBLE);
                bottomProgress.setProgress(0);
                bottomProgress.setSecondaryProgress(0);
                isLocked = false;
                mVideoView.setLock(false);
                break;
        }
    }

    protected void doLockUnlock() {
        if (isLocked) {
            isLocked = false;
            mShowing = false;
            gestureEnabled = true;
            show();
            lock.setSelected(false);
            Toast.makeText(getContext(), R.string.dkplayer_unlocked, Toast.LENGTH_SHORT).show();
        } else {
            hide();
            isLocked = true;
            gestureEnabled = false;
            lock.setSelected(true);
            Toast.makeText(getContext(), R.string.dkplayer_locked, Toast.LENGTH_SHORT).show();
        }
        mVideoView.setLock(isLocked);
    }

    /**
     * 设置是否为直播视频
     */
    public void setLive() {
        isLive = true;
        bottomProgress.setVisibility(View.GONE);
        videoProgress.setVisibility(View.INVISIBLE);
        totalTime.setVisibility(View.INVISIBLE);
        currTime.setVisibility(View.INVISIBLE);
        refresh.setVisibility(View.VISIBLE);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        isDragging = true;
        removeCallbacks(mShowProgress);
        removeCallbacks(mFadeOut);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        long duration = mVideoView.getDuration();
        long newPosition = (duration * seekBar.getProgress()) / videoProgress.getMax();
        mVideoView.seekTo((int) newPosition);
        isDragging = false;
        post(mShowProgress);
        show();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (!fromUser) {
            return;
        }

        long duration = mVideoView.getDuration();
        long newPosition = (duration * progress) / videoProgress.getMax();
        if (currTime != null)
            currTime.setText(stringForTime((int) newPosition));
    }

    @Override
    public void hide() {
        if (mShowing) {
            if (mVideoView.isFullScreen()) {
                lock.setVisibility(View.GONE);
                if (!isLocked) {
                    hideAllViews();
                }
            } else {
                bottomContainer.setVisibility(View.GONE);
                bottomContainer.startAnimation(hideAnim);
            }
            if (!isLive && !isLocked) {
                bottomProgress.setVisibility(View.VISIBLE);
                bottomProgress.startAnimation(showAnim);
            }
            mShowing = false;
        }
    }

    private void hideAllViews() {
        topContainer.setVisibility(View.GONE);
        topContainer.startAnimation(hideAnim);
        bottomContainer.setVisibility(View.GONE);
        bottomContainer.startAnimation(hideAnim);
    }

    private void show(int timeout) {
        if (sysTime != null)
            sysTime.setText(getCurrentSystemTime());
        if (!mShowing) {
            if (mVideoView.isFullScreen()) {
                lock.setVisibility(View.VISIBLE);
                if (!isLocked) {
                    showAllViews();
                }
            } else {
                bottomContainer.setVisibility(View.VISIBLE);
                bottomContainer.startAnimation(showAnim);
            }
            if (!isLocked && !isLive) {
                bottomProgress.setVisibility(View.GONE);
                bottomProgress.startAnimation(hideAnim);
            }
            mShowing = true;
        }
        removeCallbacks(mFadeOut);
        if (timeout != 0) {
            postDelayed(mFadeOut, timeout);
        }
    }

    private void showAllViews() {
        bottomContainer.setVisibility(View.VISIBLE);
        bottomContainer.startAnimation(showAnim);
        topContainer.setVisibility(View.VISIBLE);
        topContainer.startAnimation(showAnim);
    }

    @Override
    public void show() {
        show(sDefaultTimeout);
    }

    @Override
    protected int setProgress() {
        if (mVideoView == null || isDragging) {
            return 0;
        }

        if (title != null && TextUtils.isEmpty(title.getText())) {
            title.setText(mVideoView.getTitle());
        }

        if (isLive) return 0;

        int position = (int) mVideoView.getCurrentPosition();
        int duration = (int) mVideoView.getDuration();
        if (videoProgress != null) {
            if (duration > 0) {
                videoProgress.setEnabled(true);
                int pos = (int) (position * 1.0 / duration * videoProgress.getMax());
                videoProgress.setProgress(pos);
                bottomProgress.setProgress(pos);
            } else {
                videoProgress.setEnabled(false);
            }
            int percent = mVideoView.getBufferPercentage();
            if (percent >= 95) { //解决缓冲进度不能100%问题
                videoProgress.setSecondaryProgress(videoProgress.getMax());
                bottomProgress.setSecondaryProgress(bottomProgress.getMax());
            } else {
                videoProgress.setSecondaryProgress(percent * 10);
                bottomProgress.setSecondaryProgress(percent * 10);
            }
        }

        if (totalTime != null)
            totalTime.setText(stringForTime(duration));
        if (currTime != null)
            currTime.setText(stringForTime(position));

        return position;
    }


    @Override
    protected void slideToChangePosition(float deltaX) {
        if (isLive) {
            mNeedSeek = false;
        } else {
            super.slideToChangePosition(deltaX);
        }
    }

    public ImageView getThumb() {
        return thumb;
    }

    @Override
    public boolean onBackPressed() {
        if (isLocked) {
            show();
            Toast.makeText(getContext(), R.string.dkplayer_lock_tip, Toast.LENGTH_SHORT).show();
            return true;
        }
        if (mVideoView.isFullScreen()) {
            WindowUtil.scanForActivity(getContext()).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            mVideoView.stopFullScreen();
            return true;
        }
        return super.onBackPressed();
    }


    private boolean isSDCardPermissionGranted() {

        boolean sdCardWritePermissionGranted = ContextCompat.checkSelfPermission(hostPage, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;

        return sdCardWritePermissionGranted;
    }

    private void requestSDCardPermission() {

        boolean sdCardWritePermissionGranted = ContextCompat.checkSelfPermission(hostPage, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;

        final List<String> permissionList = new ArrayList();
        if (!sdCardWritePermissionGranted) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (permissionList.size() > 0 )
        {
            String[] permissionArray = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(hostPage, permissionArray, 9001);
        }
    }

    // added by betali on 2018/08/31
    @Override
    public void doScreenshot() {
        mVideoView.doScreenshot();
    }

    @Override
    public void onScreenshotComplete(int ret, String path) {

        screenshotButton.setEnabled(true);
        if (ret != 0) {

            Toast.makeText(hostPage, "截图失败", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(hostPage, "截图成功，保存路径：" + path, Toast.LENGTH_LONG).show();
    }
}
