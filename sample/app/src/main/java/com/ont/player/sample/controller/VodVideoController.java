package com.ont.player.sample.controller;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.ont.player.sample.R;


/**
 * Created by betali on 2018/3/28.
 */

public class VodVideoController extends StandardVideoController {

    public VodVideoController(@NonNull Context context) {
        super(context);
    }

    public VodVideoController(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public VodVideoController(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
