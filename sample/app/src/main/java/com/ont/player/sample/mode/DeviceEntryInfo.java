package com.ont.player.sample.mode;

/**
 * Created by betali on 2018/9/13.
 */
public class DeviceEntryInfo {

    public String mDeviceId;
    public String mChannelId;
    public String mApiKey;

    public DeviceEntryInfo() {
    }

    public DeviceEntryInfo(String deviceId, String channelId, String apiKey) {

        this.mDeviceId = deviceId;
        this.mChannelId = channelId;
        this.mApiKey = apiKey;
    }
}
