package com.ont.media.player.TimeRuler;


/**
 * 时间段
 * Created by HDL on 2017/9/4.
 */

public class TimeSlot {
    /**
     * 开始时间
     */
    public long startTime;
    /**
     * 结束时间
     */
    public long endTime;
    /**
     * 当前天数的开始时间（凌晨00:00:00）毫秒值
     */
    public long currentDayStartTimeMillis;

    /**
     * 时间段颜色类型
     */
    public byte colorType;

    public TimeSlot(long currentDayStartTimeMillis, long startTime, long endTime) {
        this.currentDayStartTimeMillis = currentDayStartTimeMillis;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public TimeSlot(long currentDayStartTimeMillis, long startTime, long endTime, byte colorType) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.currentDayStartTimeMillis = currentDayStartTimeMillis;
        this.colorType = colorType;
    }

    public long getCurrentDayStartTimeMillis() {
        return currentDayStartTimeMillis;
    }

    /**
     * 获取开始时间.
     * 当天持续秒数---->减去了当前开始时间的毫秒值（eg  00:01:00---->60）
     *
     * @return
     */
    public float getStartTime() {
        if (currentDayStartTimeMillis > startTime) {
            return 0;
        }
        return (startTime - DateUtils.getTodayStart(startTime)) / 1000f;
    }

    public long getStartTimeMillis() {
        if (currentDayStartTimeMillis > startTime) {
            return 0;
        }
        return (startTime - DateUtils.getTodayStart(startTime));
    }

    /**
     * 获取结束时间
     * 当天持续秒数---->减去了当前开始时间的毫秒值（eg  00:01:00---->60）
     *
     * @return
     */
    public float getEndTime() {
        if (currentDayStartTimeMillis + 24 * 60 * 60 * 1000 <= endTime) {
            return 24 * 60 * 60 - 1;
        }
        return (endTime - DateUtils.getTodayStart(endTime)) / 1000f;
    }

    public long getEndTimeMillis() {
        if (currentDayStartTimeMillis + 24 * 60 * 60 * 1000 <= endTime) {
            return (24 * 60 * 60 - 1) * 1000;
        }
        return (endTime - DateUtils.getTodayStart(endTime));
    }

    @Override
    public String toString() {
        return "TimeSlot{" +
                "startTime=" + getStartTime() + ",startTimeMillis=" + getStartTimeMillis() +
                ", endTime=" + getEndTime() + ",endTimeMillis=" + getEndTimeMillis() +
                '}';
    }
}
