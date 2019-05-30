package com.ont.media.player.listener;

import android.view.View;

/**
 * Created by betali on 2019/2/19.
 */
public interface OnIjkVideoViewListener {

    /**
     * 截图完成
     * @param ret
     * @param path
     */
    void onScreenshotComplete(int ret, String path);

    /**
     * 播放器回调：该协议是否支持语音推送
     */
    void onSupportPushAudio(boolean support);

    /**
     * 播放器回调：当前状态是否能开启语音推送
     */
    void onEnablePushAudio(boolean enable);

    /**
     * 播放器回调：语音推送被动停止（网络，退出播放等条件）
     */
    void onStoppedPushAudio();

    /**
     * 横屏获取右边布局
     * @return
     */
    View onGetRightExtraView();
}
