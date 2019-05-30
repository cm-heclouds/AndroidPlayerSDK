package com.ont.player.sample.controller;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ont.player.sample.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by betali on 2018/5/2.
 */

public class LiveVideoController extends StandardVideoController {


    public LiveVideoController(@NonNull Context context) {

        super(context);
    }

    public LiveVideoController(@NonNull Context context, @Nullable AttributeSet attrs) {

        super(context, attrs);
    }

    public LiveVideoController(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {

        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void initView() {

        super.initView();

        // 隐藏时间等view
        bottomProgress.setVisibility(GONE);
        videoProgress.setVisibility(INVISIBLE);
        totalTime.setVisibility(INVISIBLE);

        setLive();
    }
}
