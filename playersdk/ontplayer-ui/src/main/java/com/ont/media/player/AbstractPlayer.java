package com.ont.media.player;

import com.ont.media.player.listener.OnPlayerEventListener;

/**
 * Created by Devlin_n on 2017/12/21.
 */

public abstract class AbstractPlayer implements IPlayer {

    private OnPlayerEventListener mOnPlayerEventListener;

    /**
     * 绑定VideoView
     */
    public final void setOnPlayerEventListener(OnPlayerEventListener onPlayerEventListener) {
        this.mOnPlayerEventListener = onPlayerEventListener;
    }

    protected final void notifyOnError() {
        if (mOnPlayerEventListener != null) {
            mOnPlayerEventListener.onError();
        }
    }

    protected final void notifyOnCompletion() {
        if (mOnPlayerEventListener != null) {
            mOnPlayerEventListener.onCompletion();
        }
    }

    protected final void notifyOnInfo(int what, int extra, Object obj) {
        if (mOnPlayerEventListener != null) {
            mOnPlayerEventListener.onInfo(what, extra, obj);
        }
    }

    protected final void notifyOnPrepared() {
        if (mOnPlayerEventListener != null) {
            mOnPlayerEventListener.onPrepared();
        }
    }

    protected final void notifyOnVideoSizeChanged(int width, int height) {
        if (mOnPlayerEventListener != null) {
            mOnPlayerEventListener.onVideoSizeChanged(width, height);
        }
    }

    protected final void notifyOnScreenshotComplete(int ret, String path) {
        if (mOnPlayerEventListener != null) {
            mOnPlayerEventListener.onScreenshotComplete(ret, path);
        }
    }
}
