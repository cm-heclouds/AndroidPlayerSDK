package com.ont.media.player.TimeRuler;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.ont.media.player.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;


/**
 * 视频时间刻度尺
 * Created by HDL on 2018.2.23
 *
 * @function 刻度尺
 */
public class RulerView extends RecyclerView {

    public static long maxSpace = 180000;
    public static long oneMinute = 60;
    public static long oneHour = 60 * oneMinute;
    public static long oneDay = 24 * oneHour;

    private Context context;
    /**
     * 当前时间的毫秒值
     */
    private long currentTimeMillis;
    /**
     * 滑动结果回调
     */
    private OnBarMoveListener onBarMoveListener;
    /**
     * 线性布局
     */
    private MyLinearLayoutManager manager;
    /**
     * 屏幕的宽度
     */
    private int mScreenWidth = 0;
    /**
     * 屏幕的高度
     */
    private int mScreenHeight = 0;
    /**
     * 第一个可见item的位置
     */
    private int firstVisibleItemPosition = 0;
    /**
     * 中心点距离左边所占用的时长
     */
    private int centerPointDuration;
    /**
     * 中轴线画笔
     */
    private Paint centerLinePaint = new Paint();
    /**
     * 选择框线画笔
     */
    private Paint wheelViewPaint = new Paint();
    private int wheelViewColor = 0x27000000;//中轴线画笔颜色
    private int wheelViewWidth = CUtils.dip2px(1);

    /**
     * 调用setCurrentTimeMillis时的时间（由于currentTimeMillis随时都在变，需要记录设置时的时间来计算是否超出当天的时间）
     */
    private long dayStartMills;
    private long dayEndMills;

    private long startMills;
    private long endMills;
    private long maxCacheDuration;
    /**
     * 两小时
     */
    private static final int TWELVE_HOUR = 12 * 60 * 60 * 1000;
    /**
     * 是否是自动滑动的
     */
    private boolean isAutoScroll = true;

    /**
     * 左边屏幕的时刻
     */
    private long leftTime;
    /**
     * 适配器
     */
    private RulerAdapter adapter;

    public RulerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        if (!isInEditMode()) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.RulerView);
            setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            ta.recycle();
            init(context);
        }
    }

    private class MyLinearLayoutManager extends LinearLayoutManager {
        private boolean iscanScrollHorizontally = true;

        public MyLinearLayoutManager(Context context) {
            super(context);
        }

        @Override
        public boolean canScrollHorizontally() {
            return iscanScrollHorizontally;
        }

        public void setIscanScrollHorizontally(boolean iscanScrollHorizontally) {
            this.iscanScrollHorizontally = iscanScrollHorizontally;
        }
    }

    private void init(final Context context) {
        initPaint();
        manager = new MyLinearLayoutManager(context);
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);

        setLayoutManager(manager);
        adapter = new RulerAdapter(context);
        setAdapter(adapter);
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);

        DisplayMetrics displaymetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displaymetrics);
        mScreenWidth = displaymetrics.widthPixels;
        mScreenHeight = size.y;

        //中心点距离左边所占用的时长
        centerPointDuration = (int) ((mScreenWidth / 2f) / ((170.0 / (10 * 60 * 1000))));
        addOnScrollListener(new OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (isAutoScroll) {
                    isAutoScroll = false;
                    return;
                }
                View firstVisibleItem = manager.findViewByPosition(manager.findFirstVisibleItemPosition());
                if (firstVisibleItem == null) {
                    return;
                }
                firstVisibleItemPosition = manager.findFirstVisibleItemPosition();
                //获取左屏幕的偏移量
                int leftScrollXCalculated = (int) (Math.abs(firstVisibleItem.getLeft()) + firstVisibleItemPosition * 170);
                currentTimeMillis = (long) (dayStartMills + leftScrollXCalculated / (170.0 / (10 * 60 * 1000)) + centerPointDuration) - TWELVE_HOUR;
                //实时回调拖动时间
                if (onBarMoveListener != null) {
                    onBarMoveListener.onDragBar(dx > 0, currentTimeMillis);
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {

                super.onScrollStateChanged(recyclerView, newState);
                if (newState == 0) {

                    //滑动结束
                    isAutoScroll = true;
                    boolean exceedEndMills = false;

                    while(true) {

                        if (startMills != 0) {

                            if (startMills < 0) {

                                Calendar endDate = Calendar.getInstance();
                                endDate.setTimeInMillis(System.currentTimeMillis());
                                endDate.add(Calendar.DAY_OF_MONTH, 1);
                                endDate.set(Calendar.HOUR_OF_DAY, 0);
                                endDate.set(Calendar.MINUTE, 0);
                                endDate.set(Calendar.SECOND, 0);
                                Calendar startDate = Calendar.getInstance();
                                startDate.setTime(endDate.getTime());
                                startDate.add(Calendar.DAY_OF_MONTH, (int) (-maxCacheDuration / oneDay));

                                if (currentTimeMillis < startDate.getTimeInMillis()) {

                                    setCurrentTimeMillis(startDate.getTimeInMillis());
                                    break;
                                }
                            } else {

                                if (currentTimeMillis < startMills) {

                                    setCurrentTimeMillis(startMills);
                                    break;
                                }
                            }
                        }

                        if (endMills != 0) {

                            if (endMills < 0) {

                                long systemCurrentMills = System.currentTimeMillis();
                                if (currentTimeMillis > systemCurrentMills) {

                                    setCurrentTimeMillis(systemCurrentMills);
                                    exceedEndMills = true;
                                    break;
                                }
                            } else {

                                if (currentTimeMillis > endMills) {

                                    setCurrentTimeMillis(endMills);
                                    break;
                                }
                            }
                        }
                        break;
                    }

                    if (onBarMoveListener != null) {

                        onBarMoveListener.onBarMoveFinish(currentTimeMillis, exceedEndMills);
                    }

                    if (currentTimeMillis < dayStartMills || currentTimeMillis > dayEndMills) {

                        //跨天了
                        setCurrentTimeMillis(currentTimeMillis);
                    }
                } else {

                    //开始滑动
                    if (onBarMoveListener != null) {
                        onBarMoveListener.onBarMoveStart();
                    }
                }
            }
        });
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
    }

    public void setStartMills(long startMills) {
        this.startMills = startMills;
    }

    public void setEndMills(long endMills) {
        this.endMills = endMills;
    }

    public void setMaxCacheDuration(long maxCacheDuration) {
        this.maxCacheDuration = maxCacheDuration;
    }

    /**
     * 视频时间段集合
     */
    private List<TimeSlot> videoTimeSlot = new ArrayList<>();

    /**
     * 获取视频时间段
     *
     * @return
     */
    public List<TimeSlot> getVedioTimeSlot() {
        return videoTimeSlot;
    }

    /**
     * 设置视频时间段
     *
     * @param videoTimeSlot
     */
    public void setVideoTimeSlot(List<TimeSlot> videoTimeSlot) {
        this.videoTimeSlot.clear();
        this.videoTimeSlot.addAll(videoTimeSlot);
        adapter.setVideoTimeSlot(videoTimeSlot);
    }

    public void addVideoTimeSlot(List<TimeSlot> videoTimeSlot) {

        this.videoTimeSlot.addAll(videoTimeSlot);
        adapter.addVideoTimeSlot(videoTimeSlot);
    }

    /**
     * 跳转到今天的开始时间
     */
    private void toTodayStartPostion() {
        //计算偏移量
        int offset = getOffsetByDuration(centerPointDuration);
        manager.scrollToPositionWithOffset(12 * 6, offset);
    }

    /**
     * 跳转到今天的开始时间
     */
    private void toTodayEndPostion() {
        //计算偏移量
        int offset = getOffsetByDuration(centerPointDuration);
        manager.scrollToPositionWithOffset((12 + 24) * 6, offset);
    }

    /**
     * 根据时长计算偏移量
     *
     * @param duration
     * @return
     */
    private int getOffsetByDuration(long duration) {
        return (int) ((170f/ (10 * 60 * 1000)) * duration);
    }

    /**
     * 设置当前时间
     *
     * @param position
     */
    public synchronized void setCurrentTimeMillis(long position) {

        this.currentTimeMillis = position;
        dayStartMills = DateUtils.getTodayStart(currentTimeMillis);
        dayEndMills = DateUtils.getTodayEnd(currentTimeMillis);
        updateCenterLinePosition();
    }


    /**
     * 更新中心点的位置
     */
    public void updateCenterLinePosition() {
        //左边屏幕的时刻
        leftTime = this.currentTimeMillis - centerPointDuration;
        //根据左边时间计算第一个可以显示的下标
        int leftTimeIndex = DateUtils.getHour(leftTime) * 6 + DateUtils.getMinute(leftTime) / 10 + 12 * 6;
        if (leftTime < DateUtils.getTodayStart(currentTimeMillis)) {//跨天数了，减一天
            leftTimeIndex=leftTimeIndex - 24 * 6;
        }
        //计算偏移量
        int offset = (int) ((170f / (10 * 60 * 1000)) * DateUtils.getMinuteMillisecond(leftTime));
        //滑动到指定的item并设置偏移量(offset不能超过320px)
        manager.scrollToPositionWithOffset(leftTimeIndex, (int) (-offset % 170));
    }

    /**
     * 刻度尺移动定时器
     */
    private Timer moveTimer;

    public void moveTo(long position) {

        if (onBarMoveListener != null) {
            onBarMoveListener.onBarMoving(position);
        }
        currentTimeMillis = position;

        if (currentTimeMillis < dayStartMills || currentTimeMillis > dayEndMills) {

            dayStartMills = DateUtils.getTodayStart(currentTimeMillis);
            dayEndMills = DateUtils.getTodayEnd(currentTimeMillis);
        }
        updateCenterLinePosition();
    }

    /**
     * 初始化画笔
     */
    private void initPaint() {
        centerLinePaint.setAntiAlias(true);
        wheelViewPaint.setAntiAlias(true);
        wheelViewPaint.setStrokeWidth(wheelViewWidth);
        wheelViewPaint.setColor(wheelViewColor);
    }

    /**
     * 画中心线
     *
     * @param canvas
     */
    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        drawCenterLine(canvas);
    }

    private float selectTimeAreaDistanceLeft = -1;
    private float selectTimeAreaDistanceRight = -1;

    /**
     * 画中间线
     *
     * @param canvas
     */
    private void drawCenterLine(Canvas canvas) {
        Bitmap mBitmap1 = ((BitmapDrawable) getResources().getDrawable(R.drawable.centerline)).getBitmap();
        Bitmap mBitmap2 = ((BitmapDrawable) getResources().getDrawable(R.drawable.centerlinearrowup)).getBitmap();
        Bitmap mBitmap3 = ((BitmapDrawable) getResources().getDrawable(R.drawable.centerlinearrowdown)).getBitmap();
        canvas.drawBitmap(mBitmap1, getWidth() / 2 - mBitmap1.getWidth() / 2, 0, centerLinePaint);
        canvas.drawBitmap(mBitmap2, getWidth() / 2 - mBitmap2.getWidth() / 2, 0, centerLinePaint);
        canvas.drawBitmap(mBitmap3, getWidth() / 2 - mBitmap3.getWidth() / 2, getHeight() - CUtils.dip2px(25) - mBitmap3.getHeight(), centerLinePaint);
        canvas.drawLine(CUtils.dip2px(42), 0, CUtils.dip2px(42), CUtils.dip2px(44), wheelViewPaint);
    }

    /**
     * 设置移动监听
     *
     * @param onBarMoveListener
     */
    public void setOnBarMoveListener(OnBarMoveListener onBarMoveListener) {
        this.onBarMoveListener = onBarMoveListener;
    }

    /**
     * 拿到当前时间
     *
     * @return
     */
    public long getCurrentTimeMillis() {
        return currentTimeMillis;
    }

    private int viewHeight = CUtils.dip2px(69);
    /**
     * 设置方向
     * @param newConfig
     */
    public void setOrientation(Configuration newConfig){
        onConfigurationChanged(newConfig);
    }

    public void startFullScreen() {

        //中心点距离左边所占用的时长
        centerPointDuration = (int) ((mScreenHeight / 2f) / ((170.0/ (10 * 60 * 1000))));
        setCurrentTimeMillis(currentTimeMillis);
        postInvalidate();
    }

    public void stopFullScreen() {

        //中心点距离左边所占用的时长
        centerPointDuration = (int) ((mScreenWidth / 2f) / ((170.0/ (10 * 60 * 1000))));
        setCurrentTimeMillis(currentTimeMillis);
        postInvalidate();
    }
}
