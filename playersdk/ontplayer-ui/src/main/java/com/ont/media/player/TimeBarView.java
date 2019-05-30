package com.ont.media.player;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ont.media.player.TimeDialog.ActionListener;
import com.ont.media.player.TimeDialog.BaseDialogFragment;
import com.ont.media.player.TimeDialog.DatePickerDialog;
import com.ont.media.player.TimeRuler.DateUtils;
import com.ont.media.player.TimeRuler.OnBarMoveListener;
import com.ont.media.player.TimeRuler.RulerView;
import com.ont.media.player.TimeRuler.TimeSlot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.misc.TimeBarSlot;

/**
 * Created by betali on 2018/12/18.
 */
public class TimeBarView extends FrameLayout {

    // config
    private long maxCacheDuration;          // 最大缓存天数
    private long cacheStartSecond;  // 缓存开始时间
    private long cacheEndSecond;    // 缓存结束时间
    private long currentShowSecond; // 初始化指针显示时间
    private long lastUpdateSecond;

    // ui
    private Activity mContext;
    private LinearLayout mLayoutContainer;
    private TextView tvCurrentTime;
    private RulerView rulerView;
    private FrameLayout playLive;
    private FrameLayout dateView;
    protected IVideoView mVideoView;//播放器
    private ActionListener mActionListener;

    // data
    private boolean isInit;
    private boolean isDragging;
    private boolean isCache;
    private boolean isLive;
    private boolean isError;
    private int cacheCookie;
    private long currentStartMills;
    ArrayList<TimeSlot> timeSlots;

    public TimeBarView(@NonNull Context context) {
        super(context);
        initView(context);
    }

    public TimeBarView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public TimeBarView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public void setVideoView(IVideoView videoView) {

        this.mVideoView = videoView;
    }

    public void setMaxCacheDuration(long maxCacheDuration) {

        this.maxCacheDuration = maxCacheDuration;
    }

    public void setCacheStartSecond(long cacheStartSecond) {

        this.cacheStartSecond  = cacheStartSecond;
    }

    public void setCacheEndSecond(long cacheEndSecond) {

        this.cacheEndSecond = cacheEndSecond;
    }

    public void setCurrentShowSecond(long currentShowSecond) {

        this.currentShowSecond = currentShowSecond;
    }

    public void setPlayLive(boolean isPlayLive) {

        this.isLive = isPlayLive;
    }

    public void init() {

        timeSlots = new ArrayList<>(0);
        rulerView.setStartMills(-1);
        rulerView.setEndMills(-1);
        rulerView.setMaxCacheDuration(maxCacheDuration);
    }

    public void initTimeSlots(int count, TimeBarSlot[] timeBarSlots) {

        lastUpdateSecond = System.currentTimeMillis() / 1000;
        timeSlots.clear();
        for (int index = 0; index < count; index++) {

            long startTime = timeBarSlots[index].startTime * 1000;
            long endTime = timeBarSlots[index].endTime * 1000;
            timeSlots.add(new TimeSlot(DateUtils.getTodayStart(startTime), startTime, endTime));
        }

        if (isLive) {

            if (!isDragging) {

                long currentMills = System.currentTimeMillis();
                TimeSlot lastTimeSlot = getLastTimeSlot();
                if (lastTimeSlot != null && currentMills - lastTimeSlot.endTime < RulerView.maxSpace) {

                    rulerView.setCurrentTimeMillis(lastTimeSlot.endTime);
                    tvCurrentTime.setText(DateUtils.getDateTime(lastTimeSlot.endTime));
                } else {

                    rulerView.setCurrentTimeMillis(currentMills);
                    tvCurrentTime.setText(DateUtils.getDateTime(currentMills));
                }
            }
        } else {

            if (count > 0) {

                if (currentShowSecond > 0) {

                    long[] ret = getAnchorMills(currentShowSecond);
                    if (ret != null) {

                        currentStartMills = ret[0];
                    } else {

                        currentStartMills = 0;
                    }
                    rulerView.setCurrentTimeMillis(currentShowSecond * 1000);
                } else {

                    currentStartMills = timeSlots.get(0).startTime;
                    rulerView.setCurrentTimeMillis(currentStartMills);
                }
            } else {

                if (currentShowSecond > 0) {

                    currentStartMills = 0;
                    rulerView.setCurrentTimeMillis(currentShowSecond * 1000);
                } else {

                    currentStartMills = 0;
                    rulerView.setCurrentTimeMillis(cacheEndSecond * 500 + cacheStartSecond * 500);
                }
            }
        }

        isInit = true;
        rulerView.setVideoTimeSlot(timeSlots);
    }

    private void updateTimeSlots(long startSecond, long endSecond, int count, TimeBarSlot[] timeBarSlots, boolean waitSeek) {

        cacheStartSecond = startSecond;
        cacheEndSecond = endSecond;

        TimeSlot liveTimeSlot = null;
        if (isLive) {

            liveTimeSlot = getLiveTimeSlot();
        }

        timeSlots.clear();
        for (int index = 0; index < count; index++) {

            long startTime = timeBarSlots[index].startTime * 1000;
            long endTime = timeBarSlots[index].endTime * 1000;
            timeSlots.add(new TimeSlot(DateUtils.getTodayStart(startTime), startTime, endTime));
        }

        if (isLive && liveTimeSlot != null) {

            long leftBorderMills = 0;
            TimeSlot lastCacheTimeSlot = getLastVodTimeSlot();
            if (lastCacheTimeSlot != null) {

                leftBorderMills = lastCacheTimeSlot.endTime > liveTimeSlot.startTime ? lastCacheTimeSlot.endTime : liveTimeSlot.startTime;
            } else {

                leftBorderMills = cacheStartSecond * 1000 > liveTimeSlot.startTime ? cacheStartSecond * 1000 : liveTimeSlot.startTime;
            }

            long rightBorderMills = cacheEndSecond * 1000;
            rightBorderMills = rightBorderMills < liveTimeSlot.endTime ? rightBorderMills : liveTimeSlot.endTime;
            if (leftBorderMills < rightBorderMills) {

                long leftTodayStartMills = DateUtils.getTodayStart(leftBorderMills);
                long rightTodayStartMills = DateUtils.getTodayStart(rightBorderMills);
                if (leftTodayStartMills != rightTodayStartMills) {

                    timeSlots.add(new TimeSlot(leftTodayStartMills, leftBorderMills, rightTodayStartMills, (byte) 1));
                    timeSlots.add(new TimeSlot(rightTodayStartMills, rightTodayStartMills, rightBorderMills, (byte) 1));
                } else {

                    timeSlots.add(new TimeSlot(leftTodayStartMills, leftBorderMills, rightBorderMills, (byte) 1));
                }
            }
        }

        if (!waitSeek) {
            rulerView.setVideoTimeSlot(timeSlots);
        }
    }

    private void clearTimeSlots(boolean forward, long startSecond, long endSecond, boolean waitSeek) {

        if (forward) {

            long anchorEndSecond = startSecond + RulerView.oneDay;
            int endIndex = timeSlots.size() - 1;
            int index = endIndex;
            while (index >= 0) {

                TimeSlot item = timeSlots.get(index);
                if (item.endTime / 1000 > anchorEndSecond) {

                    index--;
                } else {

                    break;
                }
            }
            for (int i = endIndex; i > index; i--) {
                timeSlots.remove(i);
            }
        } else {

            long anchorStartSecond = endSecond - RulerView.oneDay;
            int index = 0;
            Iterator<TimeSlot> iterator = timeSlots.iterator();
            while (iterator.hasNext()){

                TimeSlot item = iterator.next();
                if (item.startTime / 1000 < anchorStartSecond) {

                    index++;
                } else {

                    break;
                }
            }
            for (int i = 0; i < index; i++) {
                timeSlots.remove(0);
            }
        }

        if (timeSlots.size() == 0) {

            cacheStartSecond = 0;
            cacheEndSecond = 0;
        } else if (forward) {

            cacheEndSecond = timeSlots.get(timeSlots.size() - 1).endTime / 1000;
        } else {

            cacheStartSecond = timeSlots.get(0).startTime / 1000;
        }

        if (!waitSeek) {
            rulerView.setVideoTimeSlot(timeSlots);
        }
    }

    public void updateCurrentMills(long reference, long progress, long delta) {

        if (reference != -1 && reference != currentStartMills) {

            currentStartMills = reference;
        }

        if (currentStartMills <= 0 || progress <= 0) {

            return;
        }

        if (isLive) {

            removeLiveTimeSlot();

            long leftBorderMills = 0;
            TimeSlot lastCacheTimeSlot = getLastVodTimeSlot();
            if (lastCacheTimeSlot != null) {

                leftBorderMills = (lastCacheTimeSlot.endTime >= currentStartMills || (currentStartMills - lastCacheTimeSlot.endTime) < RulerView.maxSpace) ? lastCacheTimeSlot.endTime : currentStartMills;
            } else {

                leftBorderMills = cacheStartSecond * 1000 > currentStartMills ? cacheStartSecond * 1000 : currentStartMills;
            }

            long rightBorderMills = currentStartMills + progress - delta;
            rightBorderMills = rightBorderMills < (cacheEndSecond * 1000) ? rightBorderMills : (cacheEndSecond * 1000);
            if (leftBorderMills < rightBorderMills) {

                long leftTodayStartMills = DateUtils.getTodayStart(leftBorderMills);
                long rightTodayStartMills = DateUtils.getTodayStart(rightBorderMills);
                if (leftTodayStartMills != rightTodayStartMills) {

                    timeSlots.add(new TimeSlot(leftTodayStartMills, leftBorderMills, rightTodayStartMills, (byte) 1));
                    timeSlots.add(new TimeSlot(rightTodayStartMills, rightTodayStartMills, rightBorderMills, (byte) 1));
                } else {

                    timeSlots.add(new TimeSlot(leftTodayStartMills, leftBorderMills, rightBorderMills, (byte) 1));
                }
                rulerView.setVideoTimeSlot(timeSlots);
            }
        }

        if (isDragging) {

            return;
        }

        if (isLive) {

            rulerView.moveTo(currentStartMills + progress - delta);
        } else {

            rulerView.moveTo(currentStartMills + progress);
        }
    }

    public PlayStatusObj onRetry() {

        currentShowSecond = rulerView.getCurrentTimeMillis() / 1000;
        return new PlayStatusObj(cacheStartSecond, cacheEndSecond, currentShowSecond, isLive);
    }

    public void setPlayState(int playState) {

        if (playState == IjkVideoView.STATE_ERROR) {

            isError = true;
        } else {

            isError = false;
        }
    }

    public void startFullScreen() {

        rulerView.startFullScreen();
    }

    public void stopFullScreen() {

        rulerView.stopFullScreen();
    }

    private void initView(Context context) {

        mContext = (Activity) context;
        final LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mLayoutContainer = (LinearLayout) inflater.inflate(R.layout.view_time_bar, null);
        this.addView(mLayoutContainer);

        dateView = findViewById(R.id.date_layout);
        dateView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                choosePicker(BaseDialogFragment.TYPE_DIALOG).show(mContext.getFragmentManager(), "dialog");
            }
        });

        mActionListener = new ActionListener() {
            @Override
            public void onCancelClick(BaseDialogFragment dialog) {}

            @Override
            public void onDoneClick(BaseDialogFragment dialog) {

                if (!isInit) {
                    return;
                }

                long currentMills = ((DatePickerDialog) dialog).getSelectedDate().getTimeInMillis();
                if (currentMills == 0) {
                    return;
                }

                rulerView.setCurrentTimeMillis(currentMills);
                tvCurrentTime.setText(DateUtils.getDateTime(currentMills));
                isDragging = true;

                if (isError) {
                    // seek
                    if (doSeek(0)) {

                        isDragging = false;
                    }
                    return;
                }

                final long currentSecond = currentMills / 1000; // second
                final boolean forward = currentSecond <= cacheStartSecond + RulerView.oneHour;
                boolean backward = (cacheEndSecond < lastUpdateSecond) ? (currentSecond >= cacheEndSecond - RulerView.oneHour) : (currentSecond >= lastUpdateSecond - RulerView.oneHour);
                if (forward || backward ) {

                    long preStartSecond = currentSecond - 12 * RulerView.oneHour;
                    long preEndSecond = currentSecond + 12 * RulerView.oneHour;
                    int ret = mVideoView.getVideoTimeSlots(++cacheCookie, preStartSecond, preEndSecond, new IMediaPlayer.IGetVideoTimeSlotCallback() {

                        @Override
                        public void OnComplete(int cookie, long startSecond, long endSecond, int count, TimeBarSlot[] timeBarSlots) {

                            if (cookie == cacheCookie) {

                                isCache = false;
                                lastUpdateSecond = System.currentTimeMillis() / 1000;
                                if (count >= 0) {
                                    updateTimeSlots(startSecond, endSecond, count, timeBarSlots, true);
                                } else {
                                    clearTimeSlots(forward, startSecond, endSecond, true);
                                }
                                cacheCookie = 0;
                                if (doSeek(currentSecond)) {

                                    removeLiveTimeSlot();
                                    isDragging = false;
                                }
                                rulerView.setVideoTimeSlot(timeSlots);
                            }
                        }
                    });
                    if (ret >= 0) {
                        isCache = true;
                        rulerView.setEnabled(false);
                    } else {

                        clearTimeSlots(forward, preStartSecond, preEndSecond, true);
                        // seek
                        if (doSeek(currentSecond)) {
                            removeLiveTimeSlot();
                            isDragging = false;
                        }
                        rulerView.setVideoTimeSlot(timeSlots);
                    }
                } else {

                    // seek
                    if (doSeek(currentSecond)) {

                        if (removeLiveTimeSlot()) {
                            rulerView.setVideoTimeSlot(timeSlots);
                        }
                        isDragging = false;
                    }
                }
            }
        };

        playLive = findViewById(R.id.play_live_layout);
        playLive.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!isInit) {
                    return;
                }

                isDragging = true;
                if (isError) {
                    if (doPlayLive()) {
                        isDragging = false;
                    }
                    return;
                }

                final long currentMills = System.currentTimeMillis();
                final long currentSecond = currentMills / 1000; // second
                final boolean forward = currentSecond <= cacheStartSecond + RulerView.oneHour;
                boolean backward;
                if (isLive) {

                    backward = currentSecond >= cacheEndSecond - RulerView.oneHour;
                } else {

                    backward = (cacheEndSecond < lastUpdateSecond) ? (currentSecond >= cacheEndSecond - RulerView.oneHour) : (currentSecond >= lastUpdateSecond - RulerView.oneHour);
                }
                if (forward || backward ) {

                    long preStartSecond = currentSecond - 12 * RulerView.oneHour;
                    long preEndSecond = currentSecond + 12 * RulerView.oneHour;
                    int ret = mVideoView.getVideoTimeSlots(++cacheCookie, preStartSecond, preEndSecond, new IMediaPlayer.IGetVideoTimeSlotCallback() {

                        @Override
                        public void OnComplete(int cookie, long startSecond, long endSecond, int count, TimeBarSlot[] timeBarSlots) {

                            if (cookie == cacheCookie) {

                                isCache = false;
                                lastUpdateSecond = System.currentTimeMillis() / 1000;
                                if (count >= 0) {
                                    updateTimeSlots(startSecond, endSecond, count, timeBarSlots, false);
                                } else {
                                    clearTimeSlots(forward, startSecond, endSecond, false);
                                }
                                cacheCookie = 0;

                                TimeSlot lastTimeSlot = getLastTimeSlot();
                                if (lastTimeSlot != null && currentMills - lastTimeSlot.endTime < RulerView.maxSpace) {

                                    rulerView.setCurrentTimeMillis(lastTimeSlot.endTime);
                                    tvCurrentTime.setText(DateUtils.getDateTime(lastTimeSlot.endTime));
                                } else {

                                    rulerView.setCurrentTimeMillis(currentMills);
                                    tvCurrentTime.setText(DateUtils.getDateTime(currentMills));
                                }
                                if (doPlayLive()) {
                                    isDragging = false;
                                }
                            }
                        }
                    });
                    if (ret >= 0) {
                        isCache = true;
                        rulerView.setEnabled(false);
                    } else {

                        clearTimeSlots(forward, preStartSecond, preEndSecond, false);
                        TimeSlot lastTimeSlot = getLastTimeSlot();
                        if (lastTimeSlot != null && currentMills - lastTimeSlot.endTime < RulerView.maxSpace) {

                            rulerView.setCurrentTimeMillis(lastTimeSlot.endTime);
                            tvCurrentTime.setText(DateUtils.getDateTime(lastTimeSlot.endTime));
                        } else {

                            rulerView.setCurrentTimeMillis(currentMills);
                            tvCurrentTime.setText(DateUtils.getDateTime(currentMills));
                        }
                        if (doPlayLive()) {
                            isDragging = false;
                        }
                    }
                } else {
                    TimeSlot lastTimeSlot = getLastTimeSlot();
                    if (lastTimeSlot != null && currentMills - lastTimeSlot.endTime < RulerView.maxSpace) {

                        rulerView.setCurrentTimeMillis(lastTimeSlot.endTime);
                        tvCurrentTime.setText(DateUtils.getDateTime(lastTimeSlot.endTime));
                    } else {

                        rulerView.setCurrentTimeMillis(currentMills);
                        tvCurrentTime.setText(DateUtils.getDateTime(currentMills));
                    }
                    if (doPlayLive()) {
                        isDragging = false;
                    }
                }
            }
        });

        rulerView = findViewById(R.id.ruler_view);
        rulerView.setOnBarMoveListener(new OnBarMoveListener() {
            @Override
            public void onDragBar(boolean isLeftDrag, long currentMills) {

                if (!isInit) {
                    return;
                }

                tvCurrentTime.setText(DateUtils.getDateTime(currentMills));
                if (isCache) {

                    return;
                }

                final long currentTime = currentMills / 1000; // second
                final boolean forward = currentTime <= cacheStartSecond + RulerView.oneHour;
                boolean backward = currentTime >= cacheEndSecond - RulerView.oneHour;
                if (!forward && !backward ) {

                    return;
                }

                long preStartSecond = currentTime - 12 * RulerView.oneHour;
                long preEndSecond = currentTime + 12 * RulerView.oneHour;
                int ret = mVideoView.getVideoTimeSlots(++cacheCookie, preStartSecond, preEndSecond, new IMediaPlayer.IGetVideoTimeSlotCallback() {

                    @Override
                    public void OnComplete(int cookie, long startSecond, long endSecond, int count, TimeBarSlot[] timeBarSlots) {

                        if (cookie == cacheCookie) {

                            isCache = false;
                            lastUpdateSecond = System.currentTimeMillis() / 1000;
                            if (count >= 0) {
                                updateTimeSlots(startSecond, endSecond, count, timeBarSlots, false);
                            } else {

                                clearTimeSlots(forward, startSecond, endSecond, false);
                            }
                            cacheCookie = 0;
                        }
                    }
                });
                if (ret >= 0) {

                    isCache = true;
                    rulerView.setEnabled(false);
                } else {

                    clearTimeSlots(forward, preStartSecond, preEndSecond, false);
                }
            }

            @Override
            public void onBarMoving(long currentMills) {

                if (!isInit) {

                    return;
                }

                tvCurrentTime.setText(DateUtils.getDateTime(currentMills));
                final long currentSecond = currentMills / 1000; // second
                final long systemSecond = System.currentTimeMillis() / 1000;
                final boolean forward = currentSecond <= cacheStartSecond + RulerView.oneHour;
                boolean backward;
                if (isLive) {

                    backward = currentSecond >= cacheEndSecond - RulerView.oneHour;
                } else {

                    if (cacheEndSecond < lastUpdateSecond) {

                        backward = currentSecond >= cacheEndSecond - RulerView.oneHour;
                    } else {

                        backward = currentSecond >= lastUpdateSecond - RulerView.oneMinute * 5;
                        backward = backward && (systemSecond - lastUpdateSecond > RulerView.oneMinute * 5);
                    }
                }

                if (!forward & !backward) {

                    return;
                }

                long preStartSecond = currentSecond - 12 * RulerView.oneHour;
                long preEndSecond = currentSecond + 12 * RulerView.oneHour;
                int ret = mVideoView.getVideoTimeSlots(++cacheCookie, preStartSecond, preEndSecond, new IMediaPlayer.IGetVideoTimeSlotCallback() {

                    @Override
                    public void OnComplete(int cookie, long startSecond, long endSecond, int count, TimeBarSlot[] timeBarSlots) {

                        if (cookie == cacheCookie) {

                            isCache = false;
                            lastUpdateSecond = System.currentTimeMillis() / 1000;
                            if (count >= 0) {
                                updateTimeSlots(startSecond, endSecond, count, timeBarSlots, false);
                            } else {
                                clearTimeSlots(forward, startSecond, endSecond, false);
                            }
                            cacheCookie = 0;
                        }
                    }
                });
                if (ret >= 0) {
                    isCache = true;
                    rulerView.setEnabled(false);
                } else {

                    clearTimeSlots(forward, preStartSecond, preEndSecond, false);
                }
            }

            @Override
            public void onBarMoveStart() {

                isDragging = true;
            }

            @Override
            public void onBarMoveFinish(long currentTime, boolean exceedEndTime) {

                if ((exceedEndTime && isLive) || !isInit) {

                    isDragging = false;
                    return;
                }

                tvCurrentTime.setText(DateUtils.getDateTime(currentTime));
                if (isError) {
                    // seek
                    if (doSeek(0)) {

                        isDragging = false;
                    }
                    return;
                }

                final long currentSecond = currentTime / 1000; // second
                final boolean forward = currentSecond <= cacheStartSecond + RulerView.oneHour;
                boolean backward = (cacheEndSecond < lastUpdateSecond) ? (currentSecond >= cacheEndSecond - RulerView.oneHour) : (currentSecond >= lastUpdateSecond - RulerView.oneHour);
                if (forward || backward) {

                    long preStartSecond = currentSecond - 12 * RulerView.oneHour;
                    long preEndSecond = currentSecond + 12 * RulerView.oneHour;
                    int ret = mVideoView.getVideoTimeSlots(++cacheCookie, preStartSecond, preEndSecond, new IMediaPlayer.IGetVideoTimeSlotCallback() {

                        @Override
                        public void OnComplete(int cookie, long startSecond, long endSecond, int count, TimeBarSlot[] timeBarSlots) {

                            if (cookie == cacheCookie) {

                                isCache = false;
                                lastUpdateSecond = System.currentTimeMillis() / 1000;
                                if (count >= 0) {
                                    updateTimeSlots(startSecond, endSecond, count, timeBarSlots, true);
                                } else {
                                    clearTimeSlots(forward, startSecond, endSecond, true);
                                }
                                cacheCookie = 0;
                                if (doSeek(currentSecond)) {
                                    removeLiveTimeSlot();
                                    isDragging = false;
                                }
                                rulerView.setVideoTimeSlot(timeSlots);
                            }
                        }
                    });
                    if (ret >= 0) {
                        isCache = true;
                        rulerView.setEnabled(false);
                    } else {

                        clearTimeSlots(forward, preStartSecond, preEndSecond, true);
                        // seek
                        if (doSeek(currentSecond)) {
                            removeLiveTimeSlot();
                            isDragging = false;
                        }
                        rulerView.setVideoTimeSlot(timeSlots);
                    }
                } else {

                    // seek
                    if (doSeek(currentSecond)) {

                        if (removeLiveTimeSlot()) {

                            rulerView.setVideoTimeSlot(timeSlots);
                        }
                        isDragging = false;
                    }
                }
            }

            @Override
            public void onMoveExceedStartTime() {

            }

            @Override
            public void onMoveExceedEndTime() {

            }

            @Override
            public void onMaxScale() {

            }

            @Override
            public void onMinScale() {

            }
        });
        tvCurrentTime = findViewById(R.id.textview);
    }

    private BaseDialogFragment choosePicker(int type) {

        DatePickerDialog picker;
        picker = DatePickerDialog.newInstance(type, mActionListener);

        // 当前系统时间
        Calendar endDate = Calendar.getInstance();
        endDate.setTime(new Date());
        endDate.set(Calendar.MINUTE, 0);
        endDate.set(Calendar.SECOND, 0);

        // 设定的最早时间
        Calendar startDate = Calendar.getInstance();
        startDate.setTime(endDate.getTime());
        startDate.add(Calendar.DAY_OF_MONTH, (int)(-maxCacheDuration / RulerView.oneDay));

        // 当前正在显示的时间
        Calendar selectedDate = Calendar.getInstance();
        selectedDate.setTimeInMillis(rulerView.getCurrentTimeMillis());
        selectedDate.set(Calendar.MINUTE, 0);
        selectedDate.set(Calendar.SECOND, 0);

        picker.setStartDate(startDate);
        picker.setEndDate(endDate);
        picker.setSelectedDate(selectedDate);
        return picker;
    }

    private boolean doSeek(long currentSecond) {

        if (isError) {

            int ret = mVideoView.seekTo(0, 0);
            if (ret == 0) {
                isLive = false;
                return true;
            }
            return false;
        }

        long currentMills = currentSecond * 1000;
        Iterator<TimeSlot> iterator = timeSlots.iterator();
        while (iterator.hasNext()) {

            TimeSlot timeSlot = iterator.next();
            if (timeSlot.colorType == 1) {
                return false;
            }
            if (currentMills > timeSlot.endTime){

                continue;
            } else if (currentMills < timeSlot.startTime) {

                return false;
            }  else {

                int ret = mVideoView.seekTo(timeSlot.startTime, currentMills - timeSlot.startTime);
                if (ret == 0) {
                    isLive = false;
                    currentStartMills = timeSlot.startTime;
                    return true;
                }
                return false;
            }
        }
        return false;
    }

    private boolean doPlayLive() {

        if (isLive) {

            mVideoView.refresh();
            return true;
        }

        int ret = mVideoView.seekTo(-1, -1);
        if (ret == 0) {
            isLive = true;
            return true;
        }
        return false;
    }


    private long[] getAnchorMills(long currentSecond) {

        long currentMills = currentSecond * 1000;
        Iterator<TimeSlot> iterator = timeSlots.iterator();
        while (iterator.hasNext()) {

            TimeSlot timeSlot = iterator.next();
            if (currentMills > timeSlot.endTime){

                continue;
            } else if (currentMills < timeSlot.startTime) {

                return null;
            }  else {

                long[] ret = {timeSlot.startTime, currentMills - timeSlot.startTime};
                return ret;
            }
        }
        return null;
    }

    private TimeSlot getLiveTimeSlot() {

        if (timeSlots.size() <= 0) {

            return null;
        }

        TimeSlot liveTimeSlot = null;
        TimeSlot lastTimeSlot;
        for (int index = timeSlots.size() - 1; index >= 0; index--) {

            lastTimeSlot = timeSlots.get(index);
            if (lastTimeSlot.colorType == 1) {

                if (liveTimeSlot == null) {

                    liveTimeSlot = lastTimeSlot;
                } else {

                    liveTimeSlot.currentDayStartTimeMillis = lastTimeSlot.currentDayStartTimeMillis;
                    liveTimeSlot.startTime = lastTimeSlot.startTime;
                }
            } else {

                break;
            }
        }
        return liveTimeSlot;
    }

    private boolean removeLiveTimeSlot() {

        if (timeSlots.size() <= 0) {

            return false;
        }

        boolean remove = false;
        TimeSlot lastTimeSlot;
        for (int index = timeSlots.size() - 1; index >= 0; index--) {

            lastTimeSlot = timeSlots.get(index);
            if (lastTimeSlot.colorType == 1) {

                remove = true;
                timeSlots.remove(index);
            } else {

                break;
            }
        }
        return remove;
    }

    private TimeSlot getLastVodTimeSlot() {

        if (timeSlots.size() <= 0) {

            return null;
        }

        TimeSlot lastTimeSlot;
        for (int index = timeSlots.size() - 1; index >= 0; index--) {

            lastTimeSlot = timeSlots.get(index);
            if (lastTimeSlot.colorType == 1) {

                continue;
            } else {

                return lastTimeSlot;
            }
        }
        return null;
    }

    private TimeSlot getLastTimeSlot() {

        if (timeSlots.size() <= 0) {

            return null;
        }

        return timeSlots.get(timeSlots.size() - 1);
    }

    public static class PlayStatusObj {

        public long cacheStartSecond;
        public long cacheEndSecond;
        public long currentShowSecond;
        public boolean isPlayLive;

        public PlayStatusObj(long cacheStartSecond, long cacheEndSecond, long currentShowSecond, boolean isPlayLive) {
            this.cacheStartSecond = cacheStartSecond;
            this.cacheEndSecond = cacheEndSecond;
            this.currentShowSecond = currentShowSecond;
            this.isPlayLive = isPlayLive;
        }
    }
}
