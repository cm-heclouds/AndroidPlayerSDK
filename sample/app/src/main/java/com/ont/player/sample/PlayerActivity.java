package com.ont.player.sample;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.ont.media.player.IjkVideoView;
import com.ont.media.player.PlayCycleConfig;
import com.ont.media.player.PlayerConfig;
import com.ont.media.player.TimeBarView;
import com.ont.media.player.TimeRuler.DateUtils;
import com.ont.media.player.controller.BaseVideoController;
import com.ont.media.player.listener.OnIjkVideoViewListener;
import com.ont.player.sample.controller.LiveVideoController;
import com.ont.player.sample.controller.StandardVideoController;

import java.util.ArrayList;
import java.util.List;

/**
 * 播放其他链接
 * Created by Devlin_n on 2017/4/7.
 */

public class PlayerActivity extends AppCompatActivity {

    private static final String TAG = "PlayerActivity";
    public static final String VIDEO_TITLE = "video_title";
    public static final String IS_LIVE = "is_live";
    public static final String IS_LOCAL = "is_local";
    public static final String PLAY_CYCLE = "play_cycle";
    public static final String API_KEY = "api_key";
    public static final String LIVE_URL = "live_url";
    public static final String TOKEN_URL = "token_url";

    public static final int AUDIO_PERMISSION = 8954;
    public static final int STORAGE_PERMISSION = 9001;

    private IjkVideoView mIjkVideoView;
    private BaseVideoController mVideoController;
    private TimeBarView mTimeBarView;
    private LinearLayout mRightExtraView;
    private ImageView mScreenshot;
    private ImageView mScreenshot_small;
    private ImageView mMicrophone;
    private ImageView mMicrophone_small;
    private boolean pushingAudio;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("OntPlayer");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mScreenshot = findViewById(R.id.screenshot_btn);
        mScreenshot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isSDCardPermissionGranted()) {

                    mIjkVideoView.doScreenshot();
                    mScreenshot.setEnabled(false);
                    mScreenshot_small.setEnabled(false);
                } else {

                    requestSDCardPermission();
                }
            }
        });

        mMicrophone = findViewById(R.id.microphone_btn);
        mMicrophone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                opPushAudio(!pushingAudio);
            }
        });

        // right extra view
        mRightExtraView = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.view_player_right_extra, null);
        mScreenshot_small = mRightExtraView.findViewById(R.id.screenshot_small_btn);
        mScreenshot_small.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isSDCardPermissionGranted()) {

                    mIjkVideoView.doScreenshot();
                    mScreenshot.setEnabled(false);
                    mScreenshot_small.setEnabled(false);
                } else {

                    requestSDCardPermission();
                }
            }
        });

        mMicrophone_small = mRightExtraView.findViewById(R.id.microphone_small_btn);
        mMicrophone_small.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                opPushAudio(!pushingAudio);
            }
        });

        // get config
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }
        final boolean isLive = intent.getBooleanExtra(IS_LIVE, false);
        boolean isLocal = intent.getBooleanExtra(IS_LOCAL, false);
        boolean playCycle = intent.getBooleanExtra(PLAY_CYCLE, false);

        // config video view
        mIjkVideoView = findViewById(R.id.player);
        mIjkVideoView.setPlayerConfig(new PlayerConfig.Builder()
                .autoRotate()//自动旋转屏幕
                .setLocalVideo(isLocal) // 设置是否本地视频流
                .enableMediaCodec()//启动硬解码
                .setPlayType(playCycle ? PlayerConfig.PlayType.TYPE_CYCLE : PlayerConfig.PlayType.TYPE_NORMAL) // 循环录制播放
                .setPlayLive(isLive)//设置是否为直播(非循环播放时生效)
                //.setScreenshotPath("/sdcard/OntRoot") // 设置截屏图保存位置，默认根目录下
                //.usingSurfaceView()//使用SurfaceView
                //.enableMediaPlayerSoftScreenshot() // 开启软解码截图功能（使用SurfaceView+软解码时才需要开启）
                .build());

        // config controller
        if (isLive) {
            mVideoController = new LiveVideoController(this);
        } else {
            mVideoController = new StandardVideoController(this);
        }
        mIjkVideoView.setVideoController(mVideoController);

        // config play cycle
        if (playCycle) {
            mTimeBarView = new TimeBarView(this);
            mIjkVideoView.setPlayCycleConfig(new PlayCycleConfig.Builder()
                    .setCacheStartSecond(DateUtils.str2num("2019-03-08 00:00:00") / 1000)
                    .setCacheEndSecond(DateUtils.str2num("2019-03-08 23:44:00") / 1000)
                    .setCurrentShowSecond(DateUtils.str2num("2019-03-08 10:19:18") / 1000)
                    .setPlayLive(true)
                    .setMaxCacheDuration(604800)
                    .setTimeoutSecond(10)
                    .setLiveUrl(intent.getStringExtra(LIVE_URL))
                    .setTokenUrl(intent.getStringExtra(TOKEN_URL))
                    .setApiKey(intent.getStringExtra(API_KEY))
                    .build());
            mIjkVideoView.setTimeBarView(mTimeBarView); // 循环录制播放时间轴
        }

        mIjkVideoView.setVideoViewListener(new OnIjkVideoViewListener() {
            @Override
            public void onScreenshotComplete(int ret, String path) {

                mScreenshot.setEnabled(true);
                mScreenshot_small.setEnabled(true);
                if (ret != 0) {

                    Toast.makeText(PlayerActivity.this, "截图失败", Toast.LENGTH_SHORT).show();
                    return;
                }

                Toast.makeText(PlayerActivity.this, "截图成功，保存路径：" + path, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSupportPushAudio(boolean support) {

                if (support && isLive) {
                    // 语音推送按钮
                    LinearLayout microphone_layout = findViewById(R.id.microphone_layout);
                    microphone_layout.setVisibility(View.VISIBLE);
                    mMicrophone_small.setVisibility(View.VISIBLE);
                } else {

                    LinearLayout microphone_layout = findViewById(R.id.microphone_layout);
                    microphone_layout.setVisibility(View.GONE);
                    mMicrophone_small.setVisibility(View.GONE);
                }
            }

            @Override
            public void onEnablePushAudio(boolean enable) {

                mMicrophone.setEnabled(enable);
                mMicrophone_small.setEnabled(enable);
            }

            @Override
            public void onStoppedPushAudio() {

                pushingAudio = false;
                mMicrophone.setImageResource(R.mipmap.microphone);
                mMicrophone_small.setImageResource(R.mipmap.microphone_small);
            }

            @Override
            public View onGetRightExtraView() {

                return mRightExtraView;
            }
        });
        String playUrl = intent.getData().toString();
        mIjkVideoView.setTitle(intent.getStringExtra(VIDEO_TITLE));
        mIjkVideoView.setUrl(playUrl);
        mIjkVideoView.start();
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

        if (requestCode == AUDIO_PERMISSION) {
            mVideoController.opPushAudio(true);
        }
    }

    public void opPushAudio(boolean start) {

        if (start && !pushingAudio) {

            if (isRecordPermissionGranted()) {

                if (mIjkVideoView.startPushAudio() == 0) {

                    pushingAudio = true;
                    mMicrophone.setImageResource(R.mipmap.microphone_light);
                    mMicrophone_small.setImageResource(R.mipmap.microphone_small_light);
                } else {
                    Toast.makeText(PlayerActivity.this, PlayerActivity.this.getString(R.string.start_audio_fail), Toast.LENGTH_SHORT).show();
                }
            } else {

                requestRecordPermission();
            }
        } else if (!start && pushingAudio) {

            pushingAudio = false;
            mMicrophone.setImageResource(R.mipmap.microphone);
            mMicrophone_small.setImageResource(R.mipmap.microphone_small);
            mIjkVideoView.stopPushAudio();
        }
    }

    private boolean isSDCardPermissionGranted() {

        boolean sdCardWritePermissionGranted = ContextCompat.checkSelfPermission(PlayerActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;

        return sdCardWritePermissionGranted;
    }

    private void requestSDCardPermission() {

        boolean sdCardWritePermissionGranted = ContextCompat.checkSelfPermission(PlayerActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;

        final List<String> permissionList = new ArrayList();
        if (!sdCardWritePermissionGranted) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (permissionList.size() > 0 )
        {
            String[] permissionArray = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(PlayerActivity.this, permissionArray, STORAGE_PERMISSION);
        }
    }

    private boolean isRecordPermissionGranted() {

        boolean microPhonePermissionGranted = ContextCompat.checkSelfPermission(PlayerActivity.this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED;

        return microPhonePermissionGranted;
    }

    private void requestRecordPermission() {

        boolean microPhonePermissionGranted = ContextCompat.checkSelfPermission(PlayerActivity.this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED;

        final List<String> permissionList = new ArrayList();
        if (!microPhonePermissionGranted) {
            permissionList.add(Manifest.permission.RECORD_AUDIO);
        }
        if (permissionList.size() > 0 )
        {
            String[] permissionArray = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(PlayerActivity.this, permissionArray, AUDIO_PERMISSION);
        }
    }
}
