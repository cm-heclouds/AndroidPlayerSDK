package com.ont.player.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.ont.media.player.IjkVideoView;
import com.ont.media.player.PlayerConfig;
import com.ont.media.player.controller.BaseVideoController;
import com.ont.player.sample.controller.LiveVideoController;
import com.ont.player.sample.controller.StandardVideoController;
import com.ont.player.sample.controller.VodVideoController;

/**
 * 播放其他链接
 * Created by Devlin_n on 2017/4/7.
 */

public class PlayerActivity extends AppCompatActivity {

    private static final String TAG = "PlayerActivity";
    public static final String VIDEO_TITLE = "video_title";
    public static final String IS_LIVE = "is_live";
    public static final String IS_LOCAL = "is_local";
    public static final String CAN_RECORD = "can_record";
    public static final String DEVICE_ID = "device_id";
    public static final String Channel_ID = "channel_id";
    public static final String API_KEY = "api_key";

    private IjkVideoView mIjkVideoView;
    private BaseVideoController mVideoController;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("OntPlayer");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        mIjkVideoView = findViewById(R.id.player);

        Intent intent = getIntent();
        if (intent != null) {

            boolean isLive = intent.getBooleanExtra(IS_LIVE, false);
            boolean isLocal = intent.getBooleanExtra(IS_LOCAL, false);
            String playUrl = intent.getData().toString();

            if (isLive) {

                mVideoController = new LiveVideoController(this);
            } else if (!isLocal) {

                mVideoController = new VodVideoController(this);
            } else {

                mVideoController = new StandardVideoController(this);
            }

            ((StandardVideoController) mVideoController).setDeviceId(intent.getStringExtra(DEVICE_ID));
            ((StandardVideoController) mVideoController).setChannelId(intent.getStringExtra(Channel_ID));
            ((StandardVideoController) mVideoController).setApiKey(intent.getStringExtra(API_KEY));
            ((StandardVideoController) mVideoController).setHostPage(this);

            mIjkVideoView.setPlayerConfig(new PlayerConfig.Builder()
                    .autoRotate()//自动旋转屏幕
//                    .setScreenshotPath("/sdcard/OntRoot") // 设置截屏图保存位置，默认根目录下
                    .setLocalVideo(isLocal) // 设置是否本地视频流
                    .enableMediaCodec()//启动硬解码
//                    .usingSurfaceView()//使用SurfaceView
//                    .enableMediaPlayerSoftScreenshot() // 开启软解码截图功能（使用SurfaceView+软解码时才需要开启）
                    .build());
            mIjkVideoView.setTitle(intent.getStringExtra(VIDEO_TITLE));
            mIjkVideoView.setUrl(playUrl);
            mIjkVideoView.setVideoController(mVideoController);
            mIjkVideoView.start();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        mVideoController.opPushAudio(true);
    }
}
