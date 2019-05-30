package com.ont.media.player;

/**
 * Created by betali on 2019/1/2.
 */
public class PlayCycleConfig {

    public long maxCacheDuration;    // 最大缓存时长
    public long onceCacheDuration;   // 一次缓存时长
    public long cacheStartSecond;    // 缓存开始时间（首次非直播时生效）
    public long cacheEndSecond;     // 缓存结束时间（首次非直播时生效）
    public long currentShowSecond;  // 指针当前时间（首次非直播时生效）
    public int timeoutSecond;       // 超时时长
    public String liveUrl;         //  拉取直播地址的API
    public String tokenUrl;        //  拉取回放token的API
    public String apiKey;          //  apiKey
    public boolean isPlayLive;     //  首次是否直播

    public PlayCycleConfig() {

    }

    public PlayCycleConfig(PlayCycleConfig origin) {

        this.maxCacheDuration = origin.maxCacheDuration;
        this.onceCacheDuration = origin.onceCacheDuration;
        this.cacheStartSecond = origin.cacheStartSecond;
        this.cacheEndSecond = origin.cacheEndSecond;
        this.currentShowSecond = origin.currentShowSecond;
        this.timeoutSecond = origin.timeoutSecond;
        this.liveUrl = origin.liveUrl;
        this.tokenUrl = origin.tokenUrl;
        this.apiKey = origin.apiKey;
        this.isPlayLive = origin.isPlayLive;
    }

    public static class Builder {

        PlayCycleConfig target;

        public Builder() {

            target = new PlayCycleConfig();
        }

        public Builder setMaxCacheDuration(long maxCacheDuration) {

            target.maxCacheDuration = maxCacheDuration;
            return this;
        }

        public Builder setOnceCacheDuration(long onceCacheDuration) {

            target.onceCacheDuration = onceCacheDuration;
            return this;
        }

        public Builder setCacheStartSecond(long cacheStartSecond) {

            target.cacheStartSecond  = cacheStartSecond;
            return this;
        }

        public Builder setCacheEndSecond(long cacheEndSecond) {

            target.cacheEndSecond = cacheEndSecond;
            return this;
        }

        public Builder setCurrentShowSecond(long currentShowSecond) {

            target.currentShowSecond = currentShowSecond;
            return this;
        }

        public Builder setTimeoutSecond(int timeoutSecond) {

            target.timeoutSecond = timeoutSecond;
            return this;
        }

        public Builder setLiveUrl(String liveUrl) {

            target.liveUrl = liveUrl;
            return this;
        }

        public Builder setTokenUrl(String tokenUrl) {

            target.tokenUrl = tokenUrl;
            return this;
        }

        public Builder setApiKey(String apiKey) {

            target.apiKey = apiKey;
            return this;
        }

        public Builder setPlayLive(boolean isPlayLive) {

            target.isPlayLive = isPlayLive;
            return this;
        }

        public PlayCycleConfig build() {

            return new PlayCycleConfig(target);
        }
    }
}
