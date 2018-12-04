package com.ont.media.player.listener;

/**
 * Created by xinyu on 2017/12/21.
 */

public interface OnPlayerEventListener {

    void onError();

    void onCompletion();

    void onInfo(int what, int extra);

    void onPrepared();

    void onVideoSizeChanged(int width, int height);

    // added by betali on 2018/08/31
    void onScreenshotComplete(int ret, String path);
}
