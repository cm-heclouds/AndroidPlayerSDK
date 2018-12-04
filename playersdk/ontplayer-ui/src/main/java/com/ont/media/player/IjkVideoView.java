package com.ont.media.player;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.TextureView;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.ont.media.player.controller.BaseVideoController;
import com.ont.media.player.listener.OnVideoViewStateChangeListener;
import com.ont.media.player.sound.AudioEncoder;
import com.ont.media.player.sound.AudioRecorder;
import com.ont.media.player.util.NetworkUtil;
import com.ont.media.player.util.PlayerConstants;
import com.ont.media.player.util.WindowUtil;
import com.ont.media.player.widget.ResizeSurfaceView;
import com.ont.media.player.widget.ResizeTextureView;

import java.io.FileOutputStream;

import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * 播放器
 * Created by Devlin_n on 2017/4/7.
 */

public class IjkVideoView extends BaseVideoView {
    protected ResizeSurfaceView mSurfaceView;
    protected ResizeTextureView mTextureView;
    protected SurfaceTexture mSurfaceTexture;
    protected FrameLayout playerContainer;
    protected boolean isFullScreen;//是否处于全屏状态

    // sound added by betali on 2018/08/31
    protected AudioEncoder mAudioEncoder;
    protected AudioRecorder mAudioRecorder;

    public static final int SCREEN_SCALE_DEFAULT = 0;
    public static final int SCREEN_SCALE_16_9 = 1;
    public static final int SCREEN_SCALE_4_3 = 2;
    public static final int SCREEN_SCALE_MATCH_PARENT = 3;
    public static final int SCREEN_SCALE_ORIGINAL = 4;
    public static final int SCREEN_SCALE_CENTER_CROP = 5;

    protected int mCurrentScreenScale = SCREEN_SCALE_DEFAULT;

    public IjkVideoView(@NonNull Context context) {
        this(context, null);
    }

    public IjkVideoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IjkVideoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }


    /**
     * 初始化播放器视图
     */
    protected void initView() {
        playerContainer = new FrameLayout(getContext());
        playerContainer.setBackgroundColor(Color.BLACK);
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        this.addView(playerContainer, params);
    }

    /**
     * 创建播放器实例，设置播放地址及播放器参数
     */
    @Override
    protected void initPlayer() {
        super.initPlayer();
        addDisplay();
    }

    protected void addDisplay() {
        if (mPlayerConfig.usingSurfaceView) {
            addSurfaceView();
        } else {
            addTextureView();
        }
    }

    /**
     * 向Controller设置播放状态，用于控制Controller的ui展示
     */
    @Override
    protected void setPlayState(int playState) {
        mCurrentPlayState = playState;
        if (mVideoController != null)
            mVideoController.setPlayState(playState);
        if (mOnVideoViewStateChangeListeners != null) {
            for (OnVideoViewStateChangeListener listener : mOnVideoViewStateChangeListeners) {
                listener.onPlayStateChanged(playState);
            }
        }
    }

    /**
     * 向Controller设置播放器状态，包含全屏状态和非全屏状态
     */
    @Override
    protected void setPlayerState(int playerState) {
        mCurrentPlayerState = playerState;
        if (mVideoController != null)
            mVideoController.setPlayerState(playerState);
        if (mOnVideoViewStateChangeListeners != null) {
            for (OnVideoViewStateChangeListener listener : mOnVideoViewStateChangeListeners) {
                listener.onPlayerStateChanged(playerState);
            }
        }
    }

    @Override
    protected void startPlay() {
        if (mPlayerConfig.addToPlayerManager) {
            VideoViewManager.instance().releaseVideoPlayer();
            VideoViewManager.instance().setCurrentVideoPlayer(this);
        }
        if (checkNetwork()) return;
        super.startPlay();
    }

    /**
     * 添加SurfaceView
     */
    private void addSurfaceView() {
        playerContainer.removeView(mSurfaceView);
        mSurfaceView = new ResizeSurfaceView(getContext());
        SurfaceHolder surfaceHolder = mSurfaceView.getHolder();
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                if (mMediaPlayer != null) {
                    mMediaPlayer.setDisplay(holder);
                }
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
            }
        });
        surfaceHolder.setFormat(PixelFormat.RGBA_8888);
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.CENTER);
        playerContainer.addView(mSurfaceView, 0, params);
    }

    /**
     * 添加TextureView
     */
    private void addTextureView() {
        playerContainer.removeView(mTextureView);
        mSurfaceTexture = null;
        mTextureView = new ResizeTextureView(getContext());
        mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
                if (mSurfaceTexture != null) {
                    mTextureView.setSurfaceTexture(mSurfaceTexture);
                } else {
                    mSurfaceTexture = surfaceTexture;
                    mMediaPlayer.setSurface(new Surface(surfaceTexture));
                }
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                return mSurfaceTexture == null;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
            }
        });
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.CENTER);
        playerContainer.addView(mTextureView, 0, params);
    }

    protected boolean checkNetwork() {
        if (mPlayerConfig.isLocalVideo) {
            return false;
        }
        if (NetworkUtil.getNetworkType(getContext()) == NetworkUtil.NETWORK_MOBILE
                && !PlayerConstants.IS_PLAY_ON_MOBILE_NETWORK) {
            if (mVideoController != null) {
                mVideoController.showStatusView();
            }
            return true;
        }
        return false;
    }

    @Override
    public void release() {
        super.release();
        playerContainer.removeView(mTextureView);
        playerContainer.removeView(mSurfaceView);
        if (mSurfaceTexture != null) {
            mSurfaceTexture.release();
            mSurfaceTexture = null;
        }
        mCurrentScreenScale = SCREEN_SCALE_DEFAULT;
    }

    /**
     * 进入全屏
     */
    @Override
    public void startFullScreen() {
        if (mVideoController == null) return;
        Activity activity = WindowUtil.scanForActivity(mVideoController.getContext());
        if (activity == null) return;
        if (isFullScreen) return;
        WindowUtil.hideSystemBar(mVideoController.getContext());
        this.removeView(playerContainer);
        ViewGroup contentView = activity
                .findViewById(android.R.id.content);
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        contentView.addView(playerContainer, params);
        orientationEventListener.enable();
        isFullScreen = true;
        setPlayerState(PLAYER_FULL_SCREEN);
    }

    /**
     * 退出全屏
     */
    @Override
    public void stopFullScreen() {
        if (mVideoController == null) return;
        Activity activity = WindowUtil.scanForActivity(mVideoController.getContext());
        if (activity == null) return;
        if (!isFullScreen) return;
        if (!mPlayerConfig.mAutoRotate) orientationEventListener.disable();
        WindowUtil.showSystemBar(mVideoController.getContext());
        ViewGroup contentView = activity
                .findViewById(android.R.id.content);
        contentView.removeView(playerContainer);
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        this.addView(playerContainer, params);
        this.requestFocus();
        isFullScreen = false;
        setPlayerState(PLAYER_NORMAL);
    }

    /**
     * 判断是否处于全屏状态
     */
    @Override
    public boolean isFullScreen() {
        return isFullScreen;
    }

    /**
     * 重试
     */
    @Override
    public void retry() {
        addDisplay();
        startPrepare(true);
    }

    @Override
    public void onInfo(int what, int extra) {
        super.onInfo(what, extra);
        switch (what) {
            case IjkMediaPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED:
                if (mTextureView != null)
                    mTextureView.setRotation(extra);
                break;
            case IjkMediaPlayer.MEDIA_INFO_STOP_WRITE_THREAD:
                stopPushAudio();
                mVideoController.onStoppedPushAudio();
                mVideoController.onEnablePushAudio(false);
                break;
            case IjkMediaPlayer.MEDIA_INFO_START_WRITE_THREAD:
                mVideoController.onSupportPushAudio(extra != 0);
                if (extra != 0) {
                    mVideoController.onEnablePushAudio(true);
                }
                break;
            case IjkMediaPlayer.MEDIA_INFO_STOPPED_PUSH_AUDIO:
                stopPushAudio();
                mVideoController.onStoppedPushAudio();
                break;
        }
    }

    @Override
    public void onVideoSizeChanged(int videoWidth, int videoHeight) {
        if (mPlayerConfig.usingSurfaceView) {
            mSurfaceView.setScreenScale(mCurrentScreenScale);
            mSurfaceView.setVideoSize(videoWidth, videoHeight);
        } else {
            mTextureView.setScreenScale(mCurrentScreenScale);
            mTextureView.setVideoSize(videoWidth, videoHeight);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && isFullScreen) {
            WindowUtil.hideSystemBar(getContext());
        }
    }

    /**
     * 设置控制器
     */
    public IjkVideoView setVideoController(@Nullable BaseVideoController mediaController) {
        playerContainer.removeView(mVideoController);
        mVideoController = mediaController;
        if (mediaController != null) {
            mediaController.setVideoView(this);
            LayoutParams params = new LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            playerContainer.addView(mVideoController, params);
        }
        return this;
    }

    /**
     * 改变返回键逻辑，用于activity
     */
    public boolean onBackPressed() {
        return mVideoController != null && mVideoController.onBackPressed();
    }

    /**
     * 设置视频比例
     */
    @Override
    public void setScreenScale(int screenScale) {
        this.mCurrentScreenScale = screenScale;
        if (mSurfaceView != null) {
            mSurfaceView.setScreenScale(screenScale);
        } else if (mTextureView != null) {
            mTextureView.setScreenScale(screenScale);
        }
    }

    /**
     * 设置镜像旋转，暂不支持SurfaceView
     */
    @Override
    public void setMirrorRotation(boolean enable) {
        if (mTextureView != null) {
            mTextureView.setScaleX(enable ? -1 : 1);
        }
    }

    // added by betali on 2018/08/31
    @Override
    public int startPushAudio() {

        // main thread
        if (mAudioEncoder == null) {

            mAudioEncoder = new AudioEncoder(this);
        }
        if (mAudioRecorder == null) {

            mAudioRecorder = new AudioRecorder(mAudioEncoder);
        }

        mAudioEncoder.setChannelConfig(mAudioRecorder.initChannelConfig());
        mAudioEncoder.start();
        mAudioRecorder.start();
        return 0;
    }

    @Override
    public int stopPushAudio() {

        if (mAudioEncoder == null || mAudioEncoder == null) {

            return 0;
        }

        mAudioRecorder.stop();
        mAudioEncoder.stop();
        return 0;
    }

    /**
     * 截图，暂不支持SurfaceView硬解码
     */
    @Override
    public void doScreenshot() {

        if (mPlayerConfig.usingSurfaceView) {

            super.doScreenshot();
        } else {

            final String filePath = mPlayerConfig.screenshotPath + "/" + System.currentTimeMillis() + ".jpg";
            new AsyncTask<String, Integer, Integer>() {

                @Override
                protected Integer doInBackground(String... params) {

                    Bitmap bitmap = mTextureView.getBitmap();
                    try {

                        FileOutputStream fos = new FileOutputStream(params[0]);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                        fos.flush();
                        fos.close();
                        return 0;
                    } catch (Exception e) {

                        e.printStackTrace();
                        return -1;
                    }
                }

                @Override
                protected void onPostExecute(Integer integer) {

                    mVideoController.onScreenshotComplete(integer, filePath);
                }
            }.execute(filePath);
        }
    }
}
