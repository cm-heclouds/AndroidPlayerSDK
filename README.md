# 中移物联网有限公司 OneNET Android Player SDK 

### 环境准备
- Android Studio 3.1.2
- Gradle 4.4
- Platform API 21~27
- NDK 16
- CPU armv5、armv7、arm64、x86、x86-64

### 特性

- 支持直播、点播
- 支持RTMP、RTMPE、HLS和HTTPS播放
- 支持RTMP，RMPE直播的语音推送
- 支持播放截屏（surfaceview硬解码场景除外）
- 优化RTMP首屏打开时间
- 优化可变帧率软解码流程，可根据帧率动态调整解码线程数

### 获取
    
```
allprojects {
    repositories {
        maven {
            url 'https://dl.bintray.com/video-onenet/maven/'
        }
    }
}

dependencies {
    implementation 'com.ont.media:ontplayer-native:1.0.0'
    implementation 'com.ont.media:ontplayer-java:1.0.0'
    implementation 'com.ont.media:ontplayer-ui:1.0.0'
}
```

### api

- player

```
@Override
protected void onCreate(@Nullable Bundle savedInstanceState) {
	mIjkVideoView.setPlayerConfig(new PlayerConfig.Builder()
		.autoRotate()//自动旋转屏幕
		.setLocalVideo(isLocal) // 设置是否本地视频流
		.enableMediaCodec() //启动硬解码
		//.usingSurfaceView() //使用SurfaceView
		//.setScreenshotPath("/sdcard/xxxxx") // 设置截屏图保存位置，默认根目录下
		//.enableMediaPlayerSoftScreenshot() // 开启软解码截图功能（使用SurfaceView+软解码时才需要开启）
	    .build());
	mIjkVideoView.setUrl(playUrl);
	mIjkVideoView.setVideoController(mVideoController);
	mIjkVideoView.start();
}

@Override
protected void onPause() {
    super.onPause();
    mIjkVideoView.pause();
}

@Override
protected void onResume() {
    super.onResume();
    mIjkVideoView.resume();
}

@Override
protected void onDestroy() {
    super.onDestroy();
    mIjkVideoView.release();
}


@Override
public void onBackPressed() {
    if (!mIjkVideoView.onBackPressed()) {
        super.onBackPressed();
    }
}
```

- audio

```
# 响应BaseVideoController中回调
@Override
public void onSupportPushAudio(boolean support) {

    if (support) {
        // 协议支持语音推送
		// ......
		// 如audioButton.setVisibility(VISIBLE);
    } else {
		// 协议不支持语音推送
		// ......
	}
}

@Override
public void onEnablePushAudio(boolean enable) {

	// 语音功能当前是否可用
	// ......
    // 如audioButton.setEnabled(enable);
}



@Override
public void onStoppedPushAudio() {

	// 语音功能已被动停止
	// ......
    // 如audioButton.setText("录音");
}
```
```
# 调用IjkVideoView中接口
public int startPushAudio() 
public int stopPushAudio() 

```
- screenshot

```
# 调用IjkVideoView中接口
public void doScreenshot();
```

```
# 实现BaseVideoController中回调
@Override
public void onScreenshotComplete(int ret, String path) {

	// 截图完成
	// ......
}
```

### License

```
Copyright (c) 2018 cmiot
Licensed under LGPLv2.1 or later
```

### 依赖库

- [Ijkplayer 0.8.8:LGPLv2.1](https://github.com/Bilibili/ijkplayer)
- [dkplayer 2.5.3:Apache License 2.0](https://github.com/dueeeke/dkplayer)




