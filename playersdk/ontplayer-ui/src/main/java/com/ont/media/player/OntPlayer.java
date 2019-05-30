package com.ont.media.player;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Surface;
import android.view.SurfaceHolder;

import java.util.Map;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class OntPlayer extends AbstractPlayer {

    protected IjkMediaPlayer mMediaPlayer;
    protected Context mAppContext;
    private int mBufferedPercent;

    public OntPlayer(Context context) {
        mAppContext = context.getApplicationContext();
    }

    @Override
    public void initPlayer() {
        mMediaPlayer = new IjkMediaPlayer();
        setOptions();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnErrorListener(onErrorListener);
        mMediaPlayer.setOnCompletionListener(onCompletionListener);
        mMediaPlayer.setOnInfoListener(onInfoListener);
        mMediaPlayer.setOnBufferingUpdateListener(onBufferingUpdateListener);
        mMediaPlayer.setOnPreparedListener(onPreparedListener);
        mMediaPlayer.setOnVideoSizeChangedListener(onVideoSizeChangedListener);
        mMediaPlayer.setOnNativeInvokeListener(new IjkMediaPlayer.OnNativeInvokeListener() {
            @Override
            public boolean onNativeInvoke(int i, Bundle bundle) {
                return true;
            }
        });
        mMediaPlayer.setOnScreenshotCompleteListener(onScreenshotCompleteListener);
    }

    @Override
    public void setOptions() {

        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzemaxduration", 100L);
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 262140L);
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "flush_packets", 1L);
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 1L);
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1L);
    }

    @Override
    public void setDataSource(String path, Map<String, String> headers) {
        try {
            Uri uri = Uri.parse(path);
            if(uri.getScheme().equals(ContentResolver.SCHEME_ANDROID_RESOURCE)){
                RawDataSourceProvider rawDataSourceProvider = RawDataSourceProvider.create(mAppContext, uri);
                mMediaPlayer.setDataSource(rawDataSourceProvider);
            } else {
                mMediaPlayer.setDataSource(mAppContext, uri, headers);
            }

        } catch (Exception e) {
            notifyOnError();
        }
    }

    @Override
    public void setDataSource(AssetFileDescriptor fd) {
        try {
            mMediaPlayer.setDataSource(fd.getFileDescriptor());
        } catch (Exception e) {
            notifyOnError();
        }
    }

    @Override
    public void pause() {
        try {
            mMediaPlayer.pause();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start() {
        mMediaPlayer.start();
    }

    @Override
    public void stop() {
        try {
            mMediaPlayer.stop();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void prepareAsync() {
        try {
            mMediaPlayer.prepareAsync();
        } catch (Exception e) {
            notifyOnError();
        }
    }

    @Override
    public void reset() {

        mMediaPlayer.reset();
        mMediaPlayer.setOnVideoSizeChangedListener(onVideoSizeChangedListener);
        setOptions();
    }

    @Override
    public boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }

    @Override
    public int seekTo(long reference, long time) {

        int ret;
        try {

            ret = mMediaPlayer.seekTo(reference, time);
        } catch (IllegalStateException e) {
            e.printStackTrace();
            ret = -1;
        }

        return ret;
    }

    @Override
    public void release() {
        if (mMediaPlayer != null)
            mMediaPlayer.release();
    }

    @Override
    public long[] getCurrentPosition() {
        return mMediaPlayer.getCurrentPosition();
    }

    @Override
    public long getDuration() {
        return mMediaPlayer.getDuration();
    }

    @Override
    public int getBufferedPercentage() {
        return mBufferedPercent;
    }

    @Override
    public void setSurface(Surface surface) {
        mMediaPlayer.setSurface(surface);
    }

    @Override
    public void setDisplay(SurfaceHolder holder) {
        mMediaPlayer.setDisplay(holder);
    }

    @Override
    public void setVolume(float v1, float v2) {
        mMediaPlayer.setVolume(v1, v2);
    }

    @Override
    public void setPlayerConfig(PlayerConfig playerConfig) {

        mMediaPlayer.setLooping(playerConfig.isLooping);
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "play-cycle", playerConfig.playType == PlayerConfig.PlayType.TYPE_CYCLE ? 1 : 0);
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "play_cycle", playerConfig.playType == PlayerConfig.PlayType.TYPE_CYCLE ? 1 : 0);
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "ffplaycodec-screenshot-path", playerConfig.enableMediaPlayerSoftScreenshot ? playerConfig.screenshotPath : "");
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "ffplaycodec-screenshot", playerConfig.enableMediaPlayerSoftScreenshot ? 1 : 0);
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", playerConfig.enableMediaCodec ? 1 : 0);//开启硬解码
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", playerConfig.enableMediaCodec ? 1 : 0);
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", playerConfig.enableMediaCodec ? 1 : 0);
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "max_cached_duration", playerConfig.maxCacheDuration);
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "play-live", playerConfig.isPlayLive ? 1 : 0);
    }

    @Override
    public void setPlayCycleConfig(PlayCycleConfig playCycleConfig) {

        if (playCycleConfig.maxCacheDuration != 0) {
            mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "max-cache-duration", playCycleConfig.maxCacheDuration);
        }
        if (playCycleConfig.onceCacheDuration != 0) {
            mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "once-cache-duration", playCycleConfig.onceCacheDuration);
        }
        if (playCycleConfig.cacheStartSecond != 0) {
            mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "i-start-time", playCycleConfig.cacheStartSecond);
        }
        if (playCycleConfig.cacheEndSecond != 0) {
            mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "i-end-time", playCycleConfig.cacheEndSecond);
        }
        if (playCycleConfig.currentShowSecond != 0) {
            mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "i-show-time", playCycleConfig.currentShowSecond);
        }
        if (playCycleConfig.timeoutSecond != 0) {
            mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "rw_timeout", playCycleConfig.timeoutSecond * 1000000);
        }
        if (!TextUtils.isEmpty(playCycleConfig.liveUrl)) {
            mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "live-url", playCycleConfig.liveUrl);
        }
        if (!TextUtils.isEmpty(playCycleConfig.tokenUrl)) {
            mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "token-url", playCycleConfig.tokenUrl);
        }
        if (!TextUtils.isEmpty(playCycleConfig.apiKey)) {
            mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "api-key", playCycleConfig.apiKey);
        }
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "play-live", playCycleConfig.isPlayLive ? 1 : 0);
    }

    @Override
    public void setSpeed(float speed) {
        mMediaPlayer.setSpeed(speed);
    }

    @Override
    public long getTcpSpeed() {
        return mMediaPlayer.getTcpSpeed();
    }

    private IMediaPlayer.OnErrorListener onErrorListener = new IMediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(IMediaPlayer iMediaPlayer, int framework_err, int impl_err) {
            notifyOnError();
            return true;
        }
    };

    private IMediaPlayer.OnCompletionListener onCompletionListener = new IMediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(IMediaPlayer iMediaPlayer) {
            notifyOnCompletion();
        }
    };

    private IMediaPlayer.OnInfoListener onInfoListener = new IMediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(IMediaPlayer iMediaPlayer, int what, int extra, Object obj) {
            notifyOnInfo(what, extra, obj);
            return true;
        }
    };

    private IMediaPlayer.OnBufferingUpdateListener onBufferingUpdateListener = new IMediaPlayer.OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(IMediaPlayer iMediaPlayer, int percent) {
            mBufferedPercent = percent;
        }
    };


    private IMediaPlayer.OnPreparedListener onPreparedListener = new IMediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(IMediaPlayer iMediaPlayer) {
            notifyOnPrepared();
        }
    };

    private IMediaPlayer.OnVideoSizeChangedListener onVideoSizeChangedListener = new IMediaPlayer.OnVideoSizeChangedListener() {
        @Override
        public void onVideoSizeChanged(IMediaPlayer iMediaPlayer, int i, int i1, int i2, int i3) {
            int videoWidth = iMediaPlayer.getVideoWidth();
            int videoHeight = iMediaPlayer.getVideoHeight();
            if (videoWidth != 0 && videoHeight != 0) {
                notifyOnVideoSizeChanged(videoWidth, videoHeight);
            }
        }
    };

    @Override
    public int writeDuplex(short pktType, byte[] buf, int size, long ts) {
        return mMediaPlayer.writeDuplex(pktType, buf, size, ts);
    }

    @Override
    public void doScreenshot() {
        mMediaPlayer.doScreenshot();
    }

    private IMediaPlayer.OnScreenshotCompleteListener onScreenshotCompleteListener = new IMediaPlayer.OnScreenshotCompleteListener() {
        @Override
        public void onScreenshotComplete(IMediaPlayer mp, int ret, String path) {
            notifyOnScreenshotComplete(ret, path);
        }
    };

    @Override
    public int getVideoTimeSlots(int cookie, long startTime, long endTime, IMediaPlayer.IGetVideoTimeSlotCallback callback) {

        return mMediaPlayer.getVideoTimeSlots(cookie, startTime, endTime, callback);
    }
}
