package com.ont.media.player;

public interface IVideoView {

    void start();

    void pause();

    long getDuration();

    long getCurrentPosition();

    void seekTo(long pos);

    boolean isPlaying();

    int getBufferPercentage();

    void startFullScreen();

    void stopFullScreen();

    boolean isFullScreen();

    String getTitle();

    void setMute(boolean isMute);

    boolean isMute();

    void setLock(boolean isLocked);

    void setScreenScale(int screenScale);

    void retry();

    void setSpeed(float speed);

    long getTcpSpeed();

    void refresh();

    void setMirrorRotation(boolean enable);

    // added by betali on 2018/08/31
    int startPushAudio();

    int stopPushAudio();

    int writeDuplex(short pktType, byte[] buf, int size, long ts);

    void doScreenshot();
}