package com.ont.media.player.TimeRuler;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by HDL on 2018/2/7.
 *
 * @author HDL
 */

public class RulerItemView extends View {
    /**
     * 刻度配置
     */
    private Paint smallRulerPaint = new Paint();//小刻度画笔
    private int rulerColor = 0x27000000;//刻度的颜色
    private int rulerWidthSamll = CUtils.dip2px(0.5f);//小刻度的宽度
    private int rulerHeightSamll = CUtils.dip2px(10);//小刻度的高度
    private static final int DEFAULT_RULER_SPACE = CUtils.dip2px(12);//设置item默认间隔
    private int rulerSpace = DEFAULT_RULER_SPACE;//刻度间的间隔
    private static final int MAX_SCALE = CUtils.dip2px(39);//最大缩放值
    private static final int MIN_SCALE = CUtils.dip2px(6);//最小缩放值
    /**
     * 大刻度
     */
    private Paint largeRulerPaint = new Paint();//大刻度画笔
    private int rulerWidthBig = CUtils.dip2px(0.5f);//大刻度的宽度
    private int rulerHeightBig = CUtils.dip2px(20);//大刻度的高度
    /**
     * 上下两条线
     */
    private Paint upAndDownLinePaint = new Paint();//刻度画笔
    private int upAndDownLineWidth = CUtils.dip2px(1);//上下两条线的宽度
    private int upAndDownLineColor = rulerColor;

    /**
     * 文本画笔
     */
    private TextPaint keyTickTextPaint = new TextPaint();
    private int textColor = 0xff444242;//文本颜色
    private int textSize = CUtils.sp2px(12);//文本大小
    /**
     * 中轴线画笔
     */
    private Paint centerLinePaint = new Paint();
    private int centerLineColor = 0xff6e9fff;//中轴线画笔
    private int centerLineWidth = CUtils.dip2px(2);
    /**
     * 视频区域画笔
     */
    private Paint videoAreaPaint = new Paint();
    private int videoBg = 0xff1bbbff;//视频背景颜色
    private Paint liveAreaPaint = new Paint();
    private int liveBg = 0x33ff6633;
    private RectF videoAreaRect = new RectF();

    /**
     * 选择时间配置
     */
    private Paint selectAreaPaint = new Paint();//选择时间边框
    private int selectTimeBorderColor = 0xfffabb64;//边框颜色
    private Paint vedioArea = new Paint();//已选时间
    private int selectTimeAreaColor = 0x33fabb64;//已选时间颜色
    private float selectTimeStrokeWidth = CUtils.dip2px(8);

    private int viewHeight = CUtils.dip2px(69);
    //除数、刻度精度
    private int divisor;

    public RulerItemView(Context context) {
        this(context, null);
    }

    public RulerItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RulerItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setDivisor();
        initPaint();
    }

    /**
     * 设置精度
     */
    private void setDivisor() {
        switch (scaleMode) {
            case KEY_HOUSE:
                divisor = 10;
                break;
            case KEY_MINUTE:
            default:
                divisor = 1;
        }
    }

    private ScaleMode scaleMode = ScaleMode.KEY_MINUTE;

    public ScaleMode getScaleMode() {
        return scaleMode;
    }

    public void setScaleMode(ScaleMode scaleMode) {
        this.scaleMode = scaleMode;
        setDivisor();
    }

    /**
     * 初始化画笔
     */
    private void initPaint() {
        smallRulerPaint.setAntiAlias(true);
        smallRulerPaint.setColor(rulerColor);
        smallRulerPaint.setStrokeWidth(rulerWidthSamll);

        largeRulerPaint.setAntiAlias(true);
        largeRulerPaint.setColor(rulerColor);
        largeRulerPaint.setStrokeWidth(rulerWidthBig);

        keyTickTextPaint.setAntiAlias(true);
        keyTickTextPaint.setColor(textColor);
        keyTickTextPaint.setTextSize(textSize);

        centerLinePaint.setAntiAlias(true);
        centerLinePaint.setStrokeWidth(centerLineWidth);
        centerLinePaint.setColor(centerLineColor);

        videoAreaPaint.setAntiAlias(true);
        videoAreaPaint.setColor(videoBg);
        liveAreaPaint.setAntiAlias(true);
        liveAreaPaint.setColor(liveBg);

        upAndDownLinePaint.setAntiAlias(true);
        upAndDownLinePaint.setColor(upAndDownLineColor);
        upAndDownLinePaint.setStrokeWidth(upAndDownLineWidth);

        selectAreaPaint.setColor(selectTimeBorderColor);
        selectAreaPaint.setAntiAlias(true);
        selectAreaPaint.setStrokeCap(Paint.Cap.ROUND);
        selectAreaPaint.setStyle(Paint.Style.STROKE);
        selectAreaPaint.setStrokeWidth(selectTimeStrokeWidth);

        vedioArea.setColor(selectTimeAreaColor);
        vedioArea.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawUpAndDownLine(canvas);
        drawRuler(canvas);
        drawVideoArea(canvas);
    }

    /**
     * 画视频区域
     *
     * @param canvas
     */
    private void drawVideoArea(Canvas canvas) {

        for (TimeSlot timeSlot : videoTimeSlot) {

            //1、首先判断是否全部包含了本时间段
            boolean isContainTime = DateUtils.isContainTime(timeSlot, timeIndex * 60 * 1000, timeIndex * 60 * 1000 + 10 * 60 * 1000);
            boolean isLeftTime = DateUtils.isCurrentTimeArea(timeSlot.getStartTimeMillis(), timeIndex * 60 * 1000, timeIndex * 60 * 1000 + 10 * 60 * 1000);
            boolean isRightTime = DateUtils.isCurrentTimeArea(timeSlot.getEndTimeMillis(), timeIndex * 60 * 1000, timeIndex * 60 * 1000 + 10 * 60 * 1000);
            if (isContainTime) {

                //包含所有（画整个item）
                videoAreaRect.set(0, 0, getWidth(), viewHeight - CUtils.dip2px(25));
                if (timeSlot.colorType == 0) {
                    canvas.drawRect(videoAreaRect, videoAreaPaint);
                } else {
                    canvas.drawRect(videoAreaRect, liveAreaPaint);
                }
            } else if (isLeftTime && isRightTime) {

                //两端都在（画左边时刻到右边时刻）
                float distanceX1 = (timeSlot.getStartTimeMillis() - timeIndex * 60 * 1000) * (getWidth() / (10 * 60 * 1000f));
                float distanceX2 = (timeSlot.getEndTimeMillis() - timeIndex * 60 * 1000) * (getWidth() / (10 * 60 * 1000f));
                videoAreaRect.set(distanceX1, 0, distanceX2, viewHeight - CUtils.dip2px(25));
                if (timeSlot.colorType == 0) {
                    canvas.drawRect(videoAreaRect, videoAreaPaint);
                } else {
                    canvas.drawRect(videoAreaRect, liveAreaPaint);
                }
            } else if (isLeftTime) {

                //只有左边在（左边时刻开始到item结束都画）
                float distanceX = (timeSlot.getStartTimeMillis() - timeIndex * 60 * 1000) * (getWidth() / (10 * 60 * 1000f));
                videoAreaRect.set(distanceX, 0, getWidth(), viewHeight - CUtils.dip2px(25));
                if (timeSlot.colorType == 0) {
                    canvas.drawRect(videoAreaRect, videoAreaPaint);
                } else {
                    canvas.drawRect(videoAreaRect, liveAreaPaint);
                }
            } else if (isRightTime) {

                //只有右边在（画从头开始到右边时刻）
                float distanceX = (timeSlot.getEndTimeMillis() - timeIndex * 60 * 1000) * (getWidth() / (10 * 60 * 1000f));
                videoAreaRect.set(0, 0, distanceX, viewHeight - CUtils.dip2px(25));
                if (timeSlot.colorType == 0) {
                    canvas.drawRect(videoAreaRect, videoAreaPaint);
                } else {
                    canvas.drawRect(videoAreaRect, liveAreaPaint);
                }
            }
        }
    }

    /**
     * 画刻度尺
     *
     * @param canvas
     */
    private void drawRuler(Canvas canvas) {
        float viewWidth = getWidth();
        float itemWidth = viewWidth / (10 / divisor);
        float rightX = 0;
        for (int i = 0; i < 60; i++) {
            if (i == 0 || i == 59) {
                if (i == 0) {
                    //画上面的大刻度
//                    canvas.drawLine(rightX, 0, rightX, rulerHeightSamll * 2, smallRulerPaint);
                    //画下面的大刻度
                    canvas.drawLine(rightX, viewHeight - CUtils.dip2px(25), rightX, viewHeight - CUtils.dip2px(21), smallRulerPaint);
                    rightX += itemWidth;
//                        ELog.e("viewHeght = " + viewHeight);
//                        ELog.e("viewHeght = " + viewHeight);
                    float timeStrWidth = keyTickTextPaint.measureText(DateUtils.getHourMinute(timeIndex));
                    canvas.drawText(DateUtils.getHourMinute(timeIndex), -timeStrWidth / 2, viewHeight - CUtils.dip2px(6), keyTickTextPaint);
                }
            }
        }
//        if (scaleMode == ScaleMode.KEY_HOUSE) {
//            //小时级别的画法
//            if ((timeIndex / 10) % 6 == 0) { //大刻度
//                //画上面的大刻度
//                canvas.drawLine(0, 0, 0, rulerHeightSamll * 2, largeRulerPaint);
//                //画下面的大刻度
//                canvas.drawLine(0, viewHeight - textSize * 2 - upAndDownLineWidth, 0, viewHeight - rulerHeightSamll * 2 - textSize * 2 - upAndDownLineWidth, largeRulerPaint);
//                float timeStrWidth = keyTickTextPaint.measureText(DateUtils.getHourMinute(timeIndex));
////                canvas.drawText(DateUtils.getHourMinute(timeIndex), timeStrWidth + rulerWidthSamll, viewHeight + CUtils.dip2px(14), keyTickTextPaint);
//                canvas.drawText(DateUtils.getHourMinute(timeIndex), -timeStrWidth / 2, viewHeight - textSize, keyTickTextPaint);
//            } else {//小刻度
//                //画上面的小刻度
//                canvas.drawLine(0, 0, 0, rulerHeightSamll, smallRulerPaint);
//                //画下面的小刻度
//                canvas.drawLine(0, viewHeight - textSize * 2 - upAndDownLineWidth, 0, viewHeight - rulerHeightSamll - textSize * 2 - upAndDownLineWidth, smallRulerPaint);
//            }
//        } else {
//            for (int i = 0; i < 60; i++) {
//                if (i == 0 || i == 59) {
//                    if (i == 0) {
//                        //画上面的大刻度
//                        canvas.drawLine(rightX, 0, rightX, rulerHeightSamll * 2, smallRulerPaint);
//                        //画下面的大刻度
//                        canvas.drawLine(rightX, viewHeight - textSize * 2 - upAndDownLineWidth, rightX, viewHeight - rulerHeightSamll * 2 - textSize * 2 - upAndDownLineWidth, smallRulerPaint);
//                        rightX += itemWidth;
////                        ELog.e("viewHeght = " + viewHeight);
////                        ELog.e("viewHeght = " + viewHeight);
//                        float timeStrWidth = keyTickTextPaint.measureText(DateUtils.getHourMinute(timeIndex));
//                        canvas.drawText(DateUtils.getHourMinute(timeIndex), -timeStrWidth / 2, viewHeight - textSize, keyTickTextPaint);
//                    }
//                } else if (i % divisor == 0) {
//                    //画上面的小刻度
//                    canvas.drawLine(rightX, 0, rightX, rulerHeightSamll, largeRulerPaint);
//                    //画下面的小刻度
//                    canvas.drawLine(rightX, viewHeight - textSize * 2 - upAndDownLineWidth, rightX, viewHeight - rulerHeightSamll - textSize * 2 - upAndDownLineWidth, largeRulerPaint);
//                    rightX += itemWidth;
//                }
//            }
//        }
    }

    /**
     * 画上下两条线
     *
     * @param canvas
     */
    private void drawUpAndDownLine(Canvas canvas) {
        int viewWidth = getWidth();
        //画上下两条线
        canvas.drawLine(0, upAndDownLineWidth / 2, viewWidth, upAndDownLineWidth / 2, upAndDownLinePaint);
        canvas.drawLine(0, viewHeight - CUtils.dip2px(25), viewWidth, viewHeight - CUtils.dip2px(25), upAndDownLinePaint);
    }

    /**
     * 设置当前时间（处于第几个小时）
     *
     * @param index
     */
    public void setCurTimeIndex(int index) {
        timeIndex = index * 10;
    }

    private int timeIndex;
    /**
     * 视频时间段集合
     */
    private List<TimeSlot> videoTimeSlot = new ArrayList<>();

    /**
     * 获取视频时间段
     *
     * @return
     */
    public List<TimeSlot> getVideoTimeSlot() {
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
        postInvalidate();//重绘
    }

//    public void setViewHeight(int viewHeight) {
//        this.viewHeight = viewHeight;
////        ELog.e("设置高度了"+viewHeight);
//        postInvalidate();
//    }
}
