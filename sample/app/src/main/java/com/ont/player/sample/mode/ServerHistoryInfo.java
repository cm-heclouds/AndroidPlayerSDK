package com.ont.player.sample.mode;

import com.ont.player.sample.IListItem;

/**
 * Created by betali on 2018/3/20.
 */

public class ServerHistoryInfo implements IListItem {

    long size;
    String videoid;
    String name;
    String start_time;
    String end_time;
    String rtmp_url;
    String hls_url;

    // customer
    boolean play_cycle;

    public String getVideoid() {
        return videoid;
    }

    public void setVideoid(String videoid) {
        this.videoid = videoid;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStart_time() {
        return start_time;
    }

    public void setStart_time(String start_time) {
        this.start_time = start_time;
    }

    public String getEnd_time() {
        return end_time;
    }

    public void setEnd_time(String end_time) {
        this.end_time = end_time;
    }

    public String getRtmp_url() {
        return rtmp_url;
    }

    public void setRtmp_url(String rtmp_url) {
        this.rtmp_url = rtmp_url;
    }

    public String getHls_url() {
        return hls_url;
    }

    public void setHls_url(String hls_url) {
        this.hls_url = hls_url;
    }

    public boolean isPlay_cycle() {
        return play_cycle;
    }

    public void setPlay_cycle(boolean play_cycle) {
        this.play_cycle = play_cycle;
    }
}
