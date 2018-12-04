package com.ont.media.player;

import android.content.res.AssetFileDescriptor;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.ont.media.player.listener.OnPlayerEventListener;

import java.util.Map;

/**
 * Created by betali on 2018/10/18.
 */
public interface IPlayer {

    /**
     * 初始化播放器实例
     */
    void initPlayer();

    /**
     * 设置播放地址
     * @param path 播放地址
     * @param headers 播放地址请求头
     */
    void setDataSource(String path, Map<String, String> headers);

    /**
     * 用于播放raw和asset里面的视频文件
     */
    void setDataSource(AssetFileDescriptor fd);

    /**
     * 播放
     */
    void start();

    /**
     * 暂停
     */
    void pause();

    /**
     * 停止
     */
    void stop();

    /**
     * 准备开始播放（异步）
     */
    void prepareAsync();

    /**
     * 重置播放器
     */
    void reset();

    /**
     * 是否正在播放
     */
    boolean isPlaying();

    /**
     * 调整进度
     */
    void seekTo(long time);

    /**
     * 释放播放器
     */
    void release();

    /**
     * 获取当前播放的位置
     */
    long getCurrentPosition();

    /**
     * 获取视频总时长
     */
    long getDuration();

    /**
     * 获取缓冲百分比
     */
    int getBufferedPercentage();

    /**
     * 设置渲染视频的View,主要用于TextureView
     */
    void setSurface(Surface surface);

    /**
     * 设置渲染视频的View,主要用于SurfaceView
     */
    void setDisplay(SurfaceHolder holder);

    /**
     * 设置音量
     */
    void setVolume(float v1, float v2);

    /**
     * 设置是否循环播放
     */
    void setLooping(boolean isLooping);

    /**
     * 设置硬解码
     */
    void setEnableMediaCodec(boolean isEnable);

    /**
     * 设置其他播放配置
     */
    void setOptions();

    /**
     * 设置播放速度
     */
    void setSpeed(float speed);

    /**
     * 获取当前缓冲的网速
     */
    long getTcpSpeed();

    /**
     * 绑定VideoView
     */
    void setOnPlayerEventListener(OnPlayerEventListener onPlayerEventListener);

    // added by betali on 2018/08/31
    /**
     * 截屏
     */
    void doScreenshot();

    /**
     * 设置截屏保存路径
     * @param path 路径
     */
    void setScreenshotPath(String path);

    /**
     * 读时写
     */
    int writeDuplex(short pktType, byte[] buf, int size, long ts);

    /**
     * 是否开启播放器软解码截屏（仅surfaceview时开启有效）
     */
    void setEnableMediaPlayerSoftScreenshot(boolean enable);
}
