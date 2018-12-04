package com.ont.player.sample.def;

import com.ont.player.sample.mode.DeviceHistoryInfo;

import java.util.List;

/**
 * Created by betali on 2018/3/20.
 */

public interface IVideoListListener {

    void onComplete(List<DeviceHistoryInfo> videoList);
}
